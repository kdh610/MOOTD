package com.example.mootd.manager

import android.view.View
import com.bumptech.glide.Glide
import com.example.mootd.databinding.FragmentMainBinding

class GuideOverlayManager(private val binding: FragmentMainBinding) {
    fun setOverlay(originalImageUrl: String?, personGuideImageUrl: String?, backgroundGuideImageUrl: String?) {
        originalImageUrl?.let {
            Glide.with(binding.root).load(it).into(binding.overlayOriginalGuide)
            binding.overlayOriginalGuide.visibility = View.VISIBLE
        }
        personGuideImageUrl?.let {
            Glide.with(binding.root).load(it).into(binding.overlayPersonGuide)
            binding.overlayPersonGuide.visibility = View.VISIBLE
        }
        backgroundGuideImageUrl?.let {
            Glide.with(binding.root).load(it).into(binding.overlayBackgroundGuide)
            binding.overlayBackgroundGuide.visibility = View.VISIBLE
        }
    }

    fun setOverlayButtons() {
        binding.btnOriginalGuide.isSelected = true
        binding.btnOriginalGuide.visibility = View.VISIBLE
        binding.btnPersonGuide.visibility = View.VISIBLE
        binding.btnBackgroundGuide.visibility = View.VISIBLE
    }

    fun clearOverlay() {
        binding.overlayOriginalGuide.visibility = View.GONE
        binding.overlayPersonGuide.visibility = View.GONE
        binding.overlayBackgroundGuide.visibility = View.GONE

        binding.btnOriginalGuide.visibility = View.GONE
        binding.btnPersonGuide.visibility = View.GONE
        binding.btnBackgroundGuide.visibility = View.GONE
    }

    fun updateOverlayImages(originalImageUrl: String?, personGuideImageUrl: String?, backgroundGuideImageUrl: String?, showOriginal: Boolean, showPerson: Boolean, showBackground: Boolean) {
        if (showOriginal && originalImageUrl != null) {
            Glide.with(binding.root).load(originalImageUrl).into(binding.overlayOriginalGuide)
            binding.overlayOriginalGuide.visibility = View.VISIBLE
        } else {
            binding.overlayOriginalGuide.visibility = View.GONE
        }

        if (showPerson && personGuideImageUrl != null) {
            Glide.with(binding.root).load(personGuideImageUrl).into(binding.overlayPersonGuide)
            binding.overlayPersonGuide.visibility = View.VISIBLE
        } else {
            binding.overlayPersonGuide.visibility = View.GONE
        }

        if (showBackground && backgroundGuideImageUrl != null) {
            Glide.with(binding.root).load(backgroundGuideImageUrl).into(binding.overlayBackgroundGuide)
            binding.overlayBackgroundGuide.visibility = View.VISIBLE
        } else {
            binding.overlayBackgroundGuide.visibility = View.GONE
        }
    }

}