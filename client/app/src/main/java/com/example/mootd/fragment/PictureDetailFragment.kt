package com.example.mootd.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
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
            CoroutineScope(Dispatchers.IO).launch {
                saveImageToInternalStorage()
            }
            findNavController().currentBackStackEntry?.savedStateHandle?.set("overlayImagePath", imagePath)
            findNavController().navigate(R.id.action_pictureDetailFragment_to_mainFragment)
        }

    }
    private suspend fun saveImageToInternalStorage() {
        val folderName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val folder = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "$folderRootPath/$folderName")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // 원본 이미지 저장
        saveImageAsync(loadBitmapFromPath(imagePath), File(folder, "original.png"))

        // 추가 이미지 데이터 저장 (예시)
        saveImageAsync(loadBitmapFromPath(imagePath), File(folder, "overlay1.png"))
        saveImageAsync(loadBitmapFromPath(imagePath), File(folder, "overlay2.png"))
        saveImageAsync(loadBitmapFromPath(imagePath), File(folder, "overlay3.png"))
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