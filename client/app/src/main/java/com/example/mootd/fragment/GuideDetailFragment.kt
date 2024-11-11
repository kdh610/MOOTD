package com.example.mootd.fragment

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mootd.R
import com.example.mootd.api.RetrofitInstance
import com.example.mootd.api.UsageRequest
import com.example.mootd.databinding.FragmentGuideDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GuideDetailFragment : Fragment() {

    private var _binding: FragmentGuideDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuideDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoId = arguments?.getString("photoId")
        val imageUrl = arguments?.getString("imageUrl")

        Log.d("argument check okhttp", "guide detail argument id: ${photoId}")

        imageUrl?.let {
            Glide.with(this).load(it).into(binding.imageView)
        }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.cameraButton.setOnClickListener{
            val deviceId = getDeviceId()
            if (deviceId != null && photoId != null) {
                postUsageData(deviceId, photoId)
            }

            val bundle = Bundle().apply {
                putString("photoId", photoId)
                putBoolean("hasGuide", true)
            }
            findNavController().navigate(R.id.action_guideDetailFragment_to_mainFragment, bundle)
        }
    }

    private fun postUsageData(deviceId: String, photoId: String) {
        val usageRequest = UsageRequest(deviceId, photoId)
        RetrofitInstance.guideUsageService.postUsageData(usageRequest).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Log.d("GuideDetailFragment", "Usage data posted successfully")
                } else {
                    Log.e("GuideDetailFragment", "Failed to post usage data: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e("GuideDetailFragment", "Error posting usage data", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getDeviceId(): String? {
        return Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}