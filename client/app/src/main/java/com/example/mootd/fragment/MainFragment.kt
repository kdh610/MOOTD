package com.example.mootd.fragment

import android.content.Context
import android.Manifest
import android.animation.ValueAnimator
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
import android.view.ScaleGestureDetector
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.addCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import com.example.mootd.api.UsageRequest
import com.example.mootd.databinding.FragmentMainBinding
import com.example.mootd.manager.CameraManager
import com.example.mootd.manager.GuideOverlayManager
import com.example.mootd.manager.GuideRecyclerManager
import com.example.mootd.viewmodel.GuideOverlayViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.max
import kotlin.math.min



class MainFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    // 목표 각도와 임계값 설정
    private val targetPitch = 0f // 목표 피치 각도
    private val targetRoll = 0f // 목표 롤 각도
    private val threshold = 5f // 허용 가능한 각도 차이


    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!


    private lateinit var cameraExecutor: ExecutorService
    private var isFrontCamera = false


    private lateinit var cameraManager: CameraManager
    private lateinit var guideOverlayManager: GuideOverlayManager
    private lateinit var guideRecyclerManager: GuideRecyclerManager

    private val guideOverlayViewModel: GuideOverlayViewModel by activityViewModels()
    private lateinit var scaleGestureDetector: ScaleGestureDetector


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Main에서는 무조건 뒤로가기 누르면 앱 꺼지게 설정
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish() // 앱 종료
        }

        cameraManager = CameraManager(this, binding)
        cameraManager.setCameraSelector(isFrontCamera)
        guideOverlayManager = GuideOverlayManager(binding)
        guideRecyclerManager = GuideRecyclerManager(requireContext(), binding, guideOverlayManager, guideOverlayViewModel) { originalUrl, personUrl, backgroundUrl ->
            guideOverlayManager.setOverlayButtons(personUrl != null)
            guideOverlayViewModel.setGuideImages(originalUrl, personUrl, backgroundUrl)
            updateOverlayImages()
        }

        if (allPermissionsGranted()) {
            cameraManager.startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        setupUI()
        loadGuideImages()
        setupNavigationButtons()


        cameraExecutor = Executors.newSingleThreadExecutor()

        guideOverlayViewModel.showOriginal.observe(viewLifecycleOwner) { showOriginal ->
            updateOverlayImages()
            binding.btnOriginalGuide.isSelected = showOriginal
        }

        guideOverlayViewModel.showPerson.observe(viewLifecycleOwner) { showPerson ->
            updateOverlayImages()
            binding.btnPersonGuide.isSelected = showPerson
        }

        guideOverlayViewModel.showBackground.observe(viewLifecycleOwner) { showBackground ->
            updateOverlayImages()
            binding.btnBackgroundGuide.isSelected = showBackground
        }

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        binding.btnSettings.setOnClickListener {
            toggleSettingsOverlay()
        }

        // 설정창 외부를 누르면 닫기
        binding.settingsOverlay.setOnClickListener {
            binding.settingsOverlay.visibility = View.GONE
        }
        binding.viewFinder.setOnClickListener {
            if(binding.horizontalLayout.visibility == View.VISIBLE) {
                binding.horizontalLayout.visibility = View.GONE
                binding.btnGuide.visibility = View.VISIBLE
                binding.btnCapture.visibility = View.VISIBLE
                binding.btnGallery.visibility = View.VISIBLE
            }
        }
        binding.settingsOverlay.findViewById<LinearLayout>(R.id.settingsContainer).setOnClickListener {
            // 내부 클릭 이벤트는 아무 동작도 하지 않음
        }
        binding.BtnCloseAllGuide.setOnClickListener {
            guideOverlayManager.clearOverlay()
            guideOverlayViewModel.clearGuideImages()
        }
        binding.seekBarOriginal.setOnSeekBarChangeListener(createSeekBarListener(binding.overlayOriginalGuide))
        binding.seekBarPerson.setOnSeekBarChangeListener(createSeekBarListener(binding.overlayPersonGuide))
        binding.seekBarBackground.setOnSeekBarChangeListener(createSeekBarListener(binding.overlayBackgroundGuide))
    }


    private fun toggleSettingsOverlay() {
        if (binding.settingsOverlay.visibility == View.GONE) {
            binding.settingsOverlay.visibility = View.VISIBLE
        } else {
            binding.settingsOverlay.visibility = View.GONE
        }
    }

    private fun createSeekBarListener(targetView: ImageView): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                targetView.alpha = progress / 100f // 0~100 값을 0.0~1.0으로 변환
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
    }

    private fun showSettingsOverlay() {
        // SeekBar의 초기값을 현재 오버레이 이미지들의 alpha 값에 맞게 설정
        binding.seekBarOriginal.progress = (binding.overlayOriginalGuide.alpha * 100).toInt()
        binding.seekBarPerson.progress = (binding.overlayPersonGuide.alpha * 100).toInt()
        binding.seekBarBackground.progress = (binding.overlayBackgroundGuide.alpha * 100).toInt()
    }


    private fun setupNavigationButtons() {
        binding.apply {
            btnMap.setOnClickListener { navigateTo(R.id.action_mainFragment_to_mapFragment) }
            btnGallery.setOnClickListener { navigateTo(R.id.action_mainFragment_to_galleryFragment) }
            btnSearch.setOnClickListener { navigateTo(R.id.action_mainFragment_to_searchFragment) }
            btnMore.setOnClickListener { navigateTo(R.id.action_mainFragment_to_guideListFragment) }
            btnCloseHorizontalLayout.setOnClickListener { toggleHorizontalLayoutVisibility(isVisible = false) }
        }
    }

    private fun navigateTo(actionId: Int) {
        findNavController().navigate(actionId)
    }

    private fun toggleHorizontalLayoutVisibility(isVisible: Boolean) {
        binding.horizontalLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.btnCapture.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.btnGallery.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.btnGuide.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    private fun setupUI() {
        binding.btnCapture.setOnClickListener {
            cameraManager.takePhoto { photoPath, isFront ->
                isFrontCamera = isFront
                val bundle = Bundle().apply {
                    putString("photoFilePath", photoPath)
                    putBoolean("isFrontCamera", isFront)
                }

                findNavController().navigate(R.id.action_mainFragment_to_pictureResultFragment, bundle)
            }
        }

        binding.btnSwitchCamera.setOnClickListener {
            isFrontCamera = !isFrontCamera
            cameraManager.toggleCamera()
        }

        setupGuideButtons()

        binding.btnGuide.setOnClickListener {
            toggleHorizontalLayoutVisibility(isVisible = true)
            guideRecyclerManager.fetchAndDisplayGuideImages()
        }
        binding.btnRetry.setOnClickListener {
            guideRecyclerManager.fetchAndDisplayGuideImages()
        }
    }

    private fun setupGuideButtons() {
        binding.btnOriginalGuide.setOnClickListener {
            guideOverlayViewModel.toggleShowOriginal()
        }

        binding.btnPersonGuide.setOnClickListener {
            guideOverlayViewModel.toggleShowPerson()
        }

        binding.btnBackgroundGuide.setOnClickListener {
            guideOverlayViewModel.toggleShowBackground()
        }
    }

    private fun updateOverlayImages() {
        guideOverlayManager.updateOverlayImages(
            guideOverlayViewModel.originalImageUrl.value,
            guideOverlayViewModel.personGuideImageUrl.value,
            guideOverlayViewModel.backgroundGuideImageUrl.value,
            guideOverlayViewModel.showOriginal.value ?: true,
            guideOverlayViewModel.showPerson.value ?: false,
            guideOverlayViewModel.showBackground.value ?: false
        )
    }

    private fun loadGuideImages() {
        if (guideOverlayViewModel.originalImageUrl.value != null) {
            guideOverlayManager.setOverlayButtons(guideOverlayViewModel.personGuideImageUrl.value != null)
            updateOverlayImages()
            showSettingsOverlay()
        }
        else {
            guideOverlayManager.clearOverlay()
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("MainFragment", "onResume called")
        // rotationSensor가 null인지 확인
        if (rotationSensor == null) {
            Log.e("MainFragment", "Rotation Sensor not available on this device!")
        } else {
            Log.d("MainFragment", "Rotation Sensor is available, registering listener.")
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
//        rotationSensor?.also {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
//        }
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Log.d("MainFragment", "Listener unregistered.")
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

//            Log.d("SensorData", "Pitch: $pitch, Roll: $roll")

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


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraManager.startCamera()
            } else {
                Toast.makeText(context, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}
