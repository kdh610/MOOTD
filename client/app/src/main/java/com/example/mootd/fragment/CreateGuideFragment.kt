package com.example.mootd.fragment

import android.os.Bundle
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


class CreateGuideFragment : Fragment() {

    private var _binding: FragmentCreateGuideBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePath: String

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

        binding.createGuideButton.setOnClickListener {
            Log.d("CreateGuideFragment", "Overlay Image Path: $imagePath")
            // 이미지 경로를 MainFragment로 전달
            findNavController().currentBackStackEntry?.savedStateHandle?.set("overlayImagePath", imagePath)
            findNavController().navigate(R.id.action_createGuideFragment_to_mainFragment)
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