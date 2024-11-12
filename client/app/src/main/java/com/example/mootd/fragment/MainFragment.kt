package com.example.mootd.fragment

import android.content.Context
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.addCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.api.GuideDetailResponse
import com.example.mootd.api.PhotoData
import com.example.mootd.api.RecentUsageResponse
import com.example.mootd.api.RetrofitInstance
import com.example.mootd.databinding.FragmentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    // 목표 각도와 임계값 설정
    private val targetPitch = 0f // 목표 피치 각도
    private val targetRoll = 0f // 목표 롤 각도
    private val threshold = 5f // 허용 가능한 각도 차이


    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var guideAdapter: GuideAdapter

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var originalImageUrl: String? = null
    private var personGuideImageUrl: String? = null
    private var backgroundGuideImageUrl: String? = null

    private var currentSelectedPhotoId: String? = null




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Main에서는 무조건 뒤로가기 누르면 앱 꺼지게
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish() // 앱 종료
        }


        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val hasGuide = arguments?.getBoolean("hasGuide") ?: false
        if (hasGuide) {
            setOverlay()
            val isLocal = arguments?.getBoolean("isLocal") ?: false
            if (isLocal) {
                originalImageUrl = arguments?.getString("originalImagePath")
                personGuideImageUrl = arguments?.getString("personGuideImagePath")
                backgroundGuideImageUrl = arguments?.getString("backgroundGuideImagePath")
            } else {
                val photoId = arguments?.getString("photoId")
                photoId?.let {
                    fetchGuideData(it)
                }
            }
        } else {
            binding.btnOriginalGuide.visibility = View.GONE
            binding.btnPersonGuide.visibility = View.GONE
            binding.btnBackgroundGuide.visibility = View.GONE
        }


        setupGuideButton()
        updateOverlayImages()

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnCapture.setOnClickListener{takePhoto()}
        binding.btnSwitchCamera.setOnClickListener{toggleCamera()}
        binding.btnMap.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_mapFragment) }
        binding.btnGallery.setOnClickListener{ findNavController().navigate(R.id.action_mainFragment_to_galleryFragment) }
        binding.btnSearch.setOnClickListener{ findNavController().navigate(R.id.action_mainFragment_to_searchFragment) }

        binding.btnMore.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_guideListFragment) }
        binding.btnCloseHorizontalLayout.setOnClickListener { binding.horizontalLayout.visibility = View.GONE }
        binding.btnGuide.setOnClickListener {
            // 가로 스크롤 사진 목록 보이기
            binding.horizontalLayout.visibility = View.VISIBLE
            fetchAndDisplayGuideImages() // 가로 스크롤 RecyclerView 설정
        }


        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            // 회전 행렬 계산
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)

            // 방향 정보 가져오기
            SensorManager.getOrientation(rotationMatrix, orientation)

            // 피치와 롤 값을 계산
            val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat() // Pitch (x-axis)
            val roll = Math.toDegrees(orientation[2].toDouble()).toFloat() // Roll (y-axis)

            // 카메라 위치 조정
            adjustCameraPosition(pitch, roll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        Log.d("Sensor", "Accuracy changed: $accuracy")
    }

    private fun adjustCameraPosition(pitch: Float, roll: Float) {

        if (Math.abs(pitch - targetPitch) > threshold || Math.abs(roll - targetRoll) > threshold) {

            Toast.makeText(context, "Adjust camera angle for better alignment.", Toast.LENGTH_SHORT)
                .show()
        }
    }



    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun fetchAndDisplayGuideImages() {
        val deviceId = getDeviceId(requireContext())

        val call = RetrofitInstance.guideRecentService.getRecentUsagePhotos(deviceId)
        call.enqueue(object : Callback<RecentUsageResponse> {
            override fun onResponse(call: Call<RecentUsageResponse>, response: Response<RecentUsageResponse>) {
                if (response.isSuccessful) {
                    val photoList = response.body()?.data ?: emptyList()
                    setupHorizontalRecyclerView(photoList) // RecyclerView 설정
                } else {
                    Log.e("API ERROR", "Response code: ${response.code()}, message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<RecentUsageResponse>, t: Throwable) {
                Log.e("API ERROR", "Network error: ${t.message}")
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupHorizontalRecyclerView(photoList: List<PhotoData>) {
        val imagePairs = photoList.map { it.photoId to it.originImageUrl }

        guideAdapter = GuideAdapter(imagePairs, R.layout.item_guide_image) { photoId ->

            if (currentSelectedPhotoId == photoId) {
                clearOverlayImages()
                currentSelectedPhotoId = null
            } else {
                currentSelectedPhotoId = photoId
                val selectedPhoto = photoList.find { it.photoId == photoId }
                setOverlay()
                // 선택한 사진 데이터를 오버레이에 설정
                selectedPhoto?.let {
                    originalImageUrl = it.originImageUrl
                    personGuideImageUrl = it.guideImageUrl
                    backgroundGuideImageUrl = it.maskImageUrl
                    updateOverlayImages() // 오버레이 이미지 업데이트
                }
            }

        }

        binding.horizontalRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = guideAdapter
            setHasFixedSize(true)
        }
    }

    private fun clearOverlayImages() {
        originalImageUrl = null
        personGuideImageUrl = null
        backgroundGuideImageUrl = null
        binding.btnOriginalGuide.isSelected = false
        updateOverlayImages() // 오버레이 이미지 업데이트


        binding.btnOriginalGuide.visibility = View.GONE
        binding.btnPersonGuide.visibility = View.GONE
        binding.btnBackgroundGuide.visibility = View.GONE
    }

    private fun setOverlay() {
        // 가이드 버튼들 표시
        binding.btnOriginalGuide.isSelected = true
        binding.btnOriginalGuide.visibility = View.VISIBLE
        binding.btnPersonGuide.visibility = View.VISIBLE
        binding.btnBackgroundGuide.visibility = View.VISIBLE
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(context, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun toggleCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(requireContext().cacheDir, "temp_photo.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // 촬영 완료 시 바로 PictureResultFragment로 전환
                    val bundle = Bundle().apply {
                        putString("photoFilePath", photoFile.absolutePath)
                        putBoolean("isFrontCamera", cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                    }
                    findNavController().navigate(R.id.action_mainFragment_to_pictureResultFragment, bundle)
                }
            }
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }




    fun fetchGuideData(photoId: String) {
        val call = RetrofitInstance.guideDetailService.getPhotoData(photoId)
        call.enqueue(object : Callback<GuideDetailResponse> {
            override fun onResponse(call: Call<GuideDetailResponse>, response: Response<GuideDetailResponse>) {
                if (response.isSuccessful) {
                    originalImageUrl = response.body()?.data?.maskImageUrl
//                    originalImageUrl = "https://mootdbucket.s3.ap-northeast-2.amazonaws.com/ORIGINAL/5c72bd20-0%ED%95%9C%EA%B0%95.jpg"
                    personGuideImageUrl = response.body()?.data?.guideImageUrl
                    backgroundGuideImageUrl = ""
//                    updateOverlayImages() // 초기 오버레이 업데이트

                    updateOverlayImages()
                } else {
                    Log.e("API ERROR", "Response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GuideDetailResponse>, t: Throwable) {
                Log.e("API ERROR", "Network error: ${t.message}")
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun updateOverlayImages() {
        Log.d("check console", "okhttp update overlay function $originalImageUrl")
        if (binding.btnOriginalGuide.isSelected) {
            originalImageUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .into(binding.overlayOriginalGuide)
                binding.overlayOriginalGuide.visibility = View.VISIBLE
            }
        } else {
            binding.overlayOriginalGuide.visibility = View.GONE
        }

        if (binding.btnPersonGuide.isSelected) {
            personGuideImageUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .into(binding.overlayPersonGuide)
                binding.overlayPersonGuide.visibility = View.VISIBLE
            }
        } else {
            binding.overlayPersonGuide.visibility = View.GONE
        }

        if (binding.btnBackgroundGuide.isSelected) {
            backgroundGuideImageUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .into(binding.overlayBackgroundGuide)
                binding.overlayBackgroundGuide.visibility = View.VISIBLE
            }
        } else {
            binding.overlayBackgroundGuide.visibility = View.GONE
        }
    }

    private fun setupGuideButton() {
        binding.btnOriginalGuide.setOnClickListener {
            binding.btnOriginalGuide.isSelected = !binding.btnOriginalGuide.isSelected
            updateOverlayImages()
        }

        binding.btnPersonGuide.setOnClickListener {
            binding.btnPersonGuide.isSelected = !binding.btnPersonGuide.isSelected
            updateOverlayImages()
        }

        binding.btnBackgroundGuide.setOnClickListener {
            binding.btnBackgroundGuide.isSelected = !binding.btnBackgroundGuide.isSelected
            updateOverlayImages()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수를 방지하기 위해 binding 해제
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}
