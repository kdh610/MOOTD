package com.example.mootd.fragment

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.mootd.R
import com.example.mootd.databinding.FragmentPictureResultBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale


class PictureResultFragment : Fragment() {

    private var _binding: FragmentPictureResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoFilePath: String


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

        CoroutineScope(Dispatchers.Main).launch {
            val previewBitmap = withContext(Dispatchers.IO) {
                getRotatedPreviewBitmap(photoFilePath)
            }
            binding.photoPreview.setImageBitmap(previewBitmap)
        }


//        val rotatedBitmap = getRotatedBitmap(photoFilePath)
//        val originalBitmap = BitmapFactory.decodeFile(photoFilePath)
//        binding.photoPreview.setImageBitmap(originalBitmap)
//
//        binding.btnSave.setOnClickListener { saveToGallery() }
//        binding.btnBack.setOnClickListener{
//            findNavController().popBackStack()
//        }
//        binding.btnShare.setOnClickListener{ sharePhoto() }
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val rotatedBitmap = getRotatedBitmap(photoFilePath)
                saveRotatedBitmap(File(photoFilePath), rotatedBitmap)
            }
        }


        binding.btnSave.setOnClickListener { saveToGallery() }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnShare.setOnClickListener { sharePhoto() }
    }

    private suspend fun getRotatedPreviewBitmap(filePath: String): Bitmap {
        return withContext(Dispatchers.IO) {
            // 1. BitmapFactory.Options로 축소된 비트맵 생성
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4 // 축소 배율을 설정하여 빠른 로딩 가능 (예: 원본의 1/4 크기)
            }
            val bitmap = BitmapFactory.decodeFile(filePath, options)

            // 2. EXIF 데이터를 통해 회전 정보 얻기
            val exif = ExifInterface(filePath)
            val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotationInDegrees = exifToDegrees(rotation)

            // 3. 회전 매트릭스를 사용하여 Bitmap 회전
            val matrix = Matrix()
            if (rotationInDegrees != 0) {
                matrix.preRotate(rotationInDegrees.toFloat())
            }

            // 축소된 비트맵을 회전시켜 반환
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }


    private suspend fun getRotatedBitmap(filePath: String): Bitmap {
        return withContext(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeFile(filePath)
            val exif = ExifInterface(filePath)
            val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotationInDegrees = exifToDegrees(rotation)
            val matrix = Matrix()
            if (rotationInDegrees != 0) {
                matrix.preRotate(rotationInDegrees.toFloat())
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }

    private suspend fun saveRotatedBitmap(file: File, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }
    }

//    private fun getRotatedBitmap(filePath: String): Bitmap {
//        val bitmap = BitmapFactory.decodeFile(filePath)
//
//        // EXIF 데이터를 통해 회전 정보 얻기
//        val exif = ExifInterface(filePath)
//        val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
//        val rotationInDegrees = exifToDegrees(rotation)
//
//        // 회전 매트릭스를 사용하여 Bitmap 회전
//        val matrix = Matrix()
//        if (rotationInDegrees != 0) {
//            matrix.preRotate(rotationInDegrees.toFloat())
//        }
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//    }

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
            resolver.openOutputStream(it)?.use { outputStream ->
                // 회전하지 않은 원본 비트맵 사용
                val originalBitmap = BitmapFactory.decodeFile(photoFilePath)
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                Toast.makeText(requireContext(), "사진이 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
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
}