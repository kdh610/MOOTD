package com.example.mootd.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mootd.R
import com.example.mootd.databinding.FragmentCreateGuideBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CreateGuideFragment : Fragment() {

    private var _binding: FragmentCreateGuideBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePath: String
    private val folderRootPath = "MyApp/GuideImages"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePath = arguments?.getString("imagePath") ?: ""

        loadImage(imagePath)

        setupToggleButton(binding.personButton)
        setupToggleButton(binding.backgroundButton)

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.createGuideButton.setOnClickListener{
            saveImageToInternalStorage()
            findNavController().navigate(R.id.action_createGuideFragment_to_mainFragment)
        }
    }

    private fun saveImageToInternalStorage() {
        val folderName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val folder = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "$folderRootPath/$folderName")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // 원본 이미지 저장
        saveImage(loadBitmapFromPath(imagePath), File(folder, "original.png"))

        // 추가 이미지 데이터 저장 (예시)
        saveImage(loadBitmapFromPath(imagePath), File(folder, "overlay1.png"))
        saveImage(loadBitmapFromPath(imagePath), File(folder, "overlay2.png"))
        saveImage(loadBitmapFromPath(imagePath), File(folder, "overlay3.png"))
    }

    private fun loadBitmapFromPath(path: String): Bitmap {
        return BitmapFactory.decodeFile(path)
    }

    private fun saveImage(bitmap: Bitmap?, file: File) {
        bitmap?.let {
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    private fun setupToggleButton(toggleButton: ToggleButton) {
        toggleButton.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked) {
                toggleButton.setTextColor(resources.getColor(R.color.main_color, null))
            } else {
                toggleButton.setTextColor(resources.getColor(R.color.gray, null))
            }
        }
    }

    private fun loadImage(imagePath: String) {
        Glide.with(this).load(imagePath).into(binding.detailImageView)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}