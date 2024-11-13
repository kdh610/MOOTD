package com.example.mootd.manager

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.api.PhotoData
import com.example.mootd.api.RecentUsageResponse
import com.example.mootd.api.RetrofitInstance
import com.example.mootd.api.UsageRequest
import com.example.mootd.databinding.FragmentMainBinding
import com.example.mootd.utils.DeviceUtils
import com.example.mootd.utils.MessageUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuideRecyclerManager(
    private val context: Context,
    private val binding: FragmentMainBinding,
    private val guideOverlayManager: GuideOverlayManager,
    private val onPhotoSelected: (originalImageUrl: String?, personGuideImageUrl: String?, backgroundGuideImageUrl: String?) -> Unit
)  {
    private var guideAdapter: GuideAdapter? = null
    private var currentSelectedPhotoId: String? = null

    fun setupRecyclerView(photoList: List<PhotoData>) {
        binding.horizontalRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.setHasFixedSize(true)

        guideAdapter = GuideAdapter(photoList.map { it.photoId to it.originImageUrl }, R.layout.item_guide_image) { photoId ->
            if (currentSelectedPhotoId == photoId) {
                clearOverlayImages()
                currentSelectedPhotoId = null
            } else {
                currentSelectedPhotoId = photoId
                val selectedPhoto = photoList.find { it.photoId == photoId }

                selectedPhoto?.let {
                    val originalImageUrl = it.originImageUrl
                    val personGuideImageUrl = it.guideImageUrl
                    val backgroundGuideImageUrl = it.maskImageUrl
                    onPhotoSelected(originalImageUrl, personGuideImageUrl, backgroundGuideImageUrl)
                    guideOverlayManager.setOverlay(originalImageUrl, personGuideImageUrl, backgroundGuideImageUrl)
                    guideOverlayManager.setOverlayButtons()
                }
                postUsageData(photoId!!)
            }
        }
        binding.horizontalRecyclerView.adapter = guideAdapter
    }

    private fun clearOverlayImages() {
        currentSelectedPhotoId = null
        guideOverlayManager.clearOverlay()
    }

    fun fetchAndDisplayGuideImages() {
        binding.tvErrorMessage.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE

        val deviceId = DeviceUtils.getDeviceId(context)
        val call = RetrofitInstance.guideRecentService.getRecentUsagePhotos(deviceId)
        call.enqueue(object : Callback<RecentUsageResponse> {
            override fun onResponse(call: Call<RecentUsageResponse>, response: Response<RecentUsageResponse>) {
                if (response.isSuccessful) {
                    val photoList = response.body()?.data ?: emptyList()
                    if (photoList.isEmpty()) {
                        MessageUtils.showNullErrorMessage(binding.tvErrorMessage)
                    } else {
                        binding.tvErrorMessage.visibility = View.GONE
                        setupRecyclerView(photoList)
                    }
                } else if (response.code() == 404) {
                    // 404 에러일 때 처리
                    MessageUtils.showNullErrorMessage(binding.tvErrorMessage)
                } else {
                    Log.e("API ERROR", "Response code: ${response.code()}, message: ${response.message()}")
                    MessageUtils.showNetworkErrorMessage(binding.tvErrorMessage, binding.btnRetry)
                }
            }

            override fun onFailure(call: Call<RecentUsageResponse>, t: Throwable) {
                Log.e("API ERROR", "Network error: ${t.message}")
                MessageUtils.showNetworkErrorMessage(binding.tvErrorMessage, binding.btnRetry)
            }
        })
    }

    fun postUsageData(photoId: String) {
        val deviceId = DeviceUtils.getDeviceId(context)
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
}