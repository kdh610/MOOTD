package com.example.mootd.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mootd.R
import com.example.mootd.databinding.FragmentPictureDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PictureDetailFragment : Fragment() {

    private var _binding: FragmentPictureDetailBinding? = null
    private val binding get() = _binding!!

    private val imagePath: String by lazy {
        arguments?.getString("imagePath") ?: ""
    }
    private val folderRootPath = "MyApp/GuideImages"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPictureDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        imagePath?.let {
            Glide.with(this)
                .load(it)
                .into(binding.detailImageView)
        }

        binding.createGuideButton.setOnClickListener{
            showLoadingOverlay()
            CoroutineScope(Dispatchers.IO).launch {
                saveImageToInternalStorage()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                hideLoadingOverlay()
                findNavController().currentBackStackEntry?.savedStateHandle?.set("overlayImagePath", imagePath)
                findNavController().navigate(R.id.action_pictureDetailFragment_to_mainFragment)
            }, 3000) // 3초 딜레이
//            findNavController().currentBackStackEntry?.savedStateHandle?.set("overlayImagePath", imagePath)
//            findNavController().navigate(R.id.action_pictureDetailFragment_to_mainFragment)
        }

    }

    private fun showLoadingOverlay() {
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
    }
    private suspend fun saveImageToInternalStorage() {
        val folderName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val folder = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "$folderRootPath/$folderName")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // 원본 이미지 저장
        saveImageAsync(loadBitmapFromPath(imagePath), File(folder, "originalImage.png"))

        // 추가 이미지 데이터 저장
        saveImageAsync(loadBitmapFromPath(imagePath), File(folder, "personGuideImage.png"))
        saveImageAsync(loadBitmapFromPath(imagePath), File(folder, "backgroundGuideImage.png"))
    }

    private fun loadBitmapFromPath(path: String): Bitmap {
        return BitmapFactory.decodeFile(path)
    }

    private suspend fun saveImageAsync(bitmap: Bitmap?, file: File) {
        withContext(Dispatchers.IO) {
            bitmap?.let {
                FileOutputStream(file).use { out ->
                    it.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}