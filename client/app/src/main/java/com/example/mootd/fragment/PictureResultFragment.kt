package com.example.mootd.fragment

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.mootd.api.RetrofitInstance
import com.example.mootd.databinding.FragmentPictureResultBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.pm.PackageManager
import android.location.Location
import android.Manifest
import com.example.mootd.api.PictureUploadResponse
import com.google.gson.Gson


class PictureResultFragment : Fragment() {

    private var _binding: FragmentPictureResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoFilePath: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPictureResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoFilePath = arguments?.getString("photoFilePath") ?: ""
        val isFrontCamera = arguments?.getBoolean("isFrontCamera") ?: false
        val deviceId = getDeviceId(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        CoroutineScope(Dispatchers.Main).launch {
            val previewBitmap = withContext(Dispatchers.IO) {
                getCorrectlyRotatedBitmap(photoFilePath, isFrontCamera)
            }
            binding.photoPreview.setImageBitmap(previewBitmap)
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        binding.btnSave.setOnClickListener {
            saveToGallery()
            if (latitude != null && longitude != null) {
                uploadPhotoToServer(File(photoFilePath), deviceId, latitude!!, longitude!!)
            } else {
                Toast.makeText(requireContext(), "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnShare.setOnClickListener { sharePhoto() }
    }

    private fun getLocation() {
        try {
            // 권한이 이미 허용된 경우 위치 가져오기
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    // 위치 정보를 여기서 사용
                } else {
                    Toast.makeText(context, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "위치 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation() // 권한이 허용된 경우 위치 정보 가져오기
            } else {
                Toast.makeText(context, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getCorrectlyRotatedBitmap(filePath: String, isFrontCamera: Boolean): Bitmap {
        return withContext(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeFile(filePath)
            val exif = ExifInterface(filePath)
            val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotationInDegrees = exifToDegrees(rotation)

            val matrix = Matrix()

            // 회전된 이미지를 정방향으로 수정
            if (rotationInDegrees != 0) {
                matrix.postRotate(rotationInDegrees.toFloat())
            }

            // 전면 카메라일 경우 좌우 반전 적용
            if (isFrontCamera) {
                matrix.postScale(-1f, 1f)
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }



    private fun exifToDegrees(rotation: Int): Int {
        return when (rotation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }


    private fun saveToGallery() {
        val photoFile = File(photoFilePath)
        val isFrontCamera = arguments?.getBoolean("isFrontCamera") ?: false
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MOOTD")
            }
        }

        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                resolver.openOutputStream(it)?.use { outputStream ->
                    // 비동기적으로 회전된 비트맵을 로드하여 저장
                    val rotatedBitmap = getCorrectlyRotatedBitmap(photoFilePath, isFrontCamera)
                    withContext(Dispatchers.Main) {
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//                        Toast.makeText(requireContext(), "사진이 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }


    private fun uploadPhotoToServer(file: File, deviceId: String, latitude: Double, longitude: Double) {
        Log.d("File Path Check",  "Check Console photoFilePath: $photoFilePath")
        val requestBodyImage = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        val imagePart = MultipartBody.Part.createFormData("originImageFile", file.name, requestBodyImage)

        val requestBodyDeviceId = RequestBody.create("text/plain".toMediaTypeOrNull(), deviceId)
        val requestBodyLatitude = RequestBody.create("text/plain".toMediaTypeOrNull(), latitude.toString())
        val requestBodyLongitude = RequestBody.create("text/plain".toMediaTypeOrNull(), longitude.toString())

        Log.d("Upload Request", "Check Console originImageFile: ${file.name}")
        Log.d("Upload Request", "Check Console deviceId: $deviceId")
        Log.d("Upload Request", "Check Console latitude: $latitude")
        Log.d("Upload Request", "Check Console longitude: $longitude")

        val call = RetrofitInstance.pictureUploadResponse.uploadPhoto(imagePart, requestBodyDeviceId, requestBodyLatitude, requestBodyLongitude)
        call.enqueue(object : Callback<PictureUploadResponse<String>> {
            override fun onResponse(call: Call<PictureUploadResponse<String>>, response: Response<PictureUploadResponse<String>>) {
                if (isAdded) {  // Fragment가 Context에 연결된 상태인지 확인
                    if (response.isSuccessful) {
                        Log.d("API Response", "Check Console Upload successful: ${response.body()}")
                        Log.d("API Response Success", "Check Console Response Code: ${response.code()}")
                        Log.d("API Response Success", "Check Console Response Body: ${Gson().toJson(response.body())}")
                        Toast.makeText(requireContext(), "업로드 성공", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("API Response", "Check Console Upload failed: ${response.errorBody()?.string()}")
                        Log.d("API Response Failure", "Check Console Response Code: ${response.code()}")
                        Log.d("API Response Failure", "Check Console Error Body: ${response.errorBody()?.string()}")
                        Toast.makeText(requireContext(), "업로드 실패: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<PictureUploadResponse<String>>, t: Throwable) {
                if (isAdded) {  // Fragment가 Context에 연결된 상태인지 확인
                    Log.e("API Error", "Check Console Network error: ${t.message}")
                    Toast.makeText(requireContext(), "네트워크 오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun sharePhoto() {
        val photoFile = File(photoFilePath)
        val uri: Uri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
        } else {
            Uri.fromFile(photoFile)
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "사진 공유하기"))
    }

    private fun deleteTempFile() {
        val tempFile = File(photoFilePath)
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        deleteTempFile()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}