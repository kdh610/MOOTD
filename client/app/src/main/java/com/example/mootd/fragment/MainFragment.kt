package com.example.mootd.fragment

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.databinding.FragmentMainBinding
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private val guideImageList = listOf(
        // 여기에 이미지 URI 또는 리소스 ID를 추가
        "https://files.oaiusercontent.com/file-10O9HgJOXxh7EYcHygK3YG6c?se=2024-11-04T06%3A14%3A37Z&sp=r&sv=2024-08-04&sr=b&rscc=max-age%3D604800%2C%20immutable%2C%20private&rscd=attachment%3B%20filename%3D186fb974-e227-4c14-a26a-cf4c426f1bbd.webp&sig=XByrKnyaQdCRKOzX8erxZwhssV4gEXJa7oES/VCJG4U%3D",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg"
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MainFragment", "onViewCreated called")
        // GuideFragment 또는 CreateGuideFragment에서 전달된 데이터 수신
        findNavController().previousBackStackEntry?.savedStateHandle?.getLiveData<String>("overlayImagePath")?.observe(viewLifecycleOwner) { imagePath ->
            Log.d("MainFragment", "Overlay Image Path?: $imagePath")
            // imagePath를 통해 오버레이 이미지 설정
            imagePath?.let {
                showOverlayImage(imagePath)
            }
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnCapture.setOnClickListener{takePhoto()}
        binding.btnSwitchCamera.setOnClickListener{toggleCamera()}
        binding.btnMap.setOnClickListener { navigateToMapFragment() }
        binding.btnGallery.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_galleryFragment)
        }

        binding.btnGuide.setOnClickListener {
            // 가로 스크롤 사진 목록 보이기
            binding.horizontalLayout.visibility = View.VISIBLE
            setupHorizontalRecyclerView() // 가로 스크롤 RecyclerView 설정
        }


        binding.btnMore.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_guideListFragment)
        }

        binding.btnCloseHorizontalLayout.setOnClickListener{
            binding.horizontalLayout.post {
                binding.horizontalLayout.visibility = View.GONE
            }
        }

        binding.btnCloseHorizontalLayout.post {
            val parentView = binding.horizontalLayout
            val delegateArea = Rect()
            binding.btnCloseHorizontalLayout.getHitRect(delegateArea)
            val extraPadding = 50 // 원하는 크기만큼 클릭 영역을 확장 (px 단위)
            delegateArea.inset(-extraPadding, -extraPadding)
            parentView.touchDelegate = TouchDelegate(delegateArea, binding.btnCloseHorizontalLayout)
        }

    }

    private fun setupHorizontalRecyclerView() {
        binding.horizontalRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = GuideAdapter(guideImageList, R.layout.item_guide_image) { imageUri ->
                // 클릭 이벤트 처리
                // 예: 클릭한 이미지 URI를 로그로 출력하거나, 다른 화면으로 이동하는 코드 작성
                println("Image clicked: $imageUri")
            }
            setHasFixedSize(true)
        }
    }

    private fun navigateToMapFragment() {
        findNavController().navigate(R.id.action_mainFragment_to_mapFragment)
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
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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
        // imageCapture 객체가 null인 경우 바로 return
        val imageCapture = imageCapture ?: return

        // 임시 파일 생성
        val photoFile = File(requireContext().cacheDir, "temp_photo.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()



        // 사진 촬영 후 결과를 처리하는 리스너 설정
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                    val rotatedBitmap = getRotatedBitmap(photoFile.absolutePath)
                    saveRotatedBitmap(photoFile, rotatedBitmap)

                    val bundle = Bundle().apply {
                        putString("photoFilePath", photoFile.absolutePath)
                    }
                    findNavController().navigate(R.id.action_mainFragment_to_pictureResultFragment, bundle)
                }
            }
        )
    }

    private fun saveRotatedBitmap(file: File, bitmap: Bitmap) {
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
    }

    private fun getRotatedBitmap(filePath: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(filePath)

        // EXIF 데이터를 통해 회전 정보 얻기
        val exif = ExifInterface(filePath)
        val rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotationInDegrees = exifToDegrees(rotation)

        // 회전 매트릭스를 사용하여 Bitmap 회전
        val matrix = Matrix()
        if (rotationInDegrees != 0) {
            matrix.preRotate(rotationInDegrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun exifToDegrees(rotation: Int): Int {
        return when (rotation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수를 방지하기 위해 binding 해제
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    fun showOverlayImage(imagePath: String) {
        Log.d("MainFragment", "showOverlayImage called with path: $imagePath")
        val bitmap = BitmapFactory.decodeFile(imagePath)

        if (bitmap != null) {
            Log.d("MainFragment", "Loading image from path: $imagePath")
            binding.overlayImage.setImageBitmap(bitmap)
            binding.overlayImage.visibility = View.VISIBLE
        } else {
            Log.e("MainFragment", "Failed to load image: Bitmap is null for path $imagePath")
        }
    }

    fun hideOverlayImage() {
        binding.overlayImage.visibility = View.GONE
    }
}