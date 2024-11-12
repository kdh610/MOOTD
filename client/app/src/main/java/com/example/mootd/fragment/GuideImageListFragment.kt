package com.example.mootd.fragment

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsAnimation
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.api.RecentUsageResponse
import com.example.mootd.api.RetrofitInstance
import com.example.mootd.api.UsageRequest
import com.example.mootd.databinding.FragmentGuideImageListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class GuideImageListFragment : Fragment() {

    private var _binding: FragmentGuideImageListBinding? = null
    private val binding get() = _binding!!

    private lateinit var guideAdapter: GuideAdapter
    private lateinit var deviceId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuideImageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listType = arguments?.getString(ARG_LIST_TYPE)

        if (listType == "new") {
            // 'new' 타입일 때 폴더에서 이미지를 가져옴
            val imageList = getGuideListFromFolders().map { null to it }
            guideAdapter = GuideAdapter(imageList, R.layout.item_gallery_image) { imageUri ->
                imageUri?.let { navigateToMainFragmentWithLocalImages(it) }
            }
            binding.verticalRecyclerView.adapter = guideAdapter
        } else {
            // 'recent' 타입일 때 API에서 이미지를 가져옴
            guideAdapter = GuideAdapter(emptyList(), R.layout.item_gallery_image) { photoId  ->
                photoId?.let {
                    navigateToMainFragmentWithApiData(it)
                    postUsageData(it)
                }
            }
            binding.verticalRecyclerView.adapter = guideAdapter
            getRecentGuideList()  // API 호출
        }

        binding.verticalRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = guideAdapter
            setHasFixedSize(true)
        }
    }
    private fun navigateToMainFragmentWithLocalImages(imageUri: String) {
        val folderPath = File(imageUri).parentFile // 폴더 경로 가져오기
        val originalImagePath = File(folderPath, "originalImage.png").absolutePath
        val personGuidePath = File(folderPath, "personGuideImage.png").absolutePath
        val backgroundGuidePath = File(folderPath, "backgroundGuideImage.png").absolutePath

        val bundle = Bundle().apply {
            putString("originalImagePath", originalImagePath)
            putString("personGuideImagePath", personGuidePath)
            putString("backgroundGuideImagePath", backgroundGuidePath)
            putBoolean("isLocal", true)
            putBoolean("hasGuide", true)
        }
        findNavController().navigate(R.id.action_guideListFragment_to_mainFragment, bundle)
    }

    private fun navigateToMainFragmentWithApiData(photoId: String) {
        val bundle = Bundle().apply {
            putString("photoId", photoId)
            putBoolean("isLocal", false)
            putBoolean("hasGuide", true)
        }
        findNavController().navigate(R.id.action_guideListFragment_to_mainFragment, bundle)
    }



    private fun getGuideListFromFolders(): List<String> {
        val imageList = mutableListOf<String>()
        val rootDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyApp/GuideImages")

        rootDir.listFiles()?.sortedByDescending { it.lastModified() }?.forEach { folder ->
            if (folder.isDirectory) {
                val originalFile = File(folder, "originalImage.png")
                if (originalFile.exists()) {
                    imageList.add(originalFile.absolutePath)
                }
            }
        }
        return imageList
    }

    private fun getRecentGuideList() {
        deviceId = getDeviceId(requireContext())

        val call = RetrofitInstance.guideRecentService.getRecentUsagePhotos(deviceId)
        call.enqueue(object : Callback<RecentUsageResponse> {
            override fun onResponse(call: Call<RecentUsageResponse>, response: Response<RecentUsageResponse>) {
                if (response.isSuccessful) {
                    val recentData = response.body()?.data?.mapNotNull { data ->
                        data.photoId to (data.originImageUrl ?: "")
                    } ?: emptyList()
                    if (recentData != null) {
                        guideAdapter.updateData(recentData)
                    }
                } else {
                    Log.d("API ERROR", "ERROR: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<RecentUsageResponse>, t: Throwable) {
                // 네트워크 오류 시 처리할 코드
            }
        })
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun postUsageData(photoId: String) {
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



    companion object {
        private const val ARG_LIST_TYPE = "list_type"

        fun newInstance(listType: String): GuideImageListFragment {
            val fragment = GuideImageListFragment()
            val args = Bundle()
            args.putString(ARG_LIST_TYPE, listType)
            fragment.arguments = args
            return fragment
        }
    }


}