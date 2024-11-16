package com.example.mootd.fragment

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.adapter.UnifiedPhotoData
import com.example.mootd.api.RecentUsageResponse
import com.example.mootd.api.RetrofitInstance
import com.example.mootd.api.UsageRequest
import com.example.mootd.databinding.FragmentGuideImageListBinding
import com.example.mootd.utils.DeviceUtils
import com.example.mootd.utils.MessageUtils
import com.example.mootd.viewmodel.GuideOverlayViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class GuideImageListFragment : Fragment() {

    private var _binding: FragmentGuideImageListBinding? = null
    private val binding get() = _binding!!

    private lateinit var guideAdapter: GuideAdapter
    private lateinit var deviceId: String

    private val guideOverlayViewModel: GuideOverlayViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuideImageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvErrorMessage.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
        val listType = arguments?.getString(ARG_LIST_TYPE)

        if (listType == "new") {
            // 'new' 타입일 때 폴더에서 이미지를 가져옴
            val imageList = getGuideListFromFolders()
            guideAdapter = GuideAdapter(imageList, R.layout.item_gallery_image) { photoData ->
                guideOverlayViewModel.setGuideImages(
                    photoData.originalImageUrl,
                    photoData.personGuidelineUrl,
                    photoData.backgroundGuidelineUrl
                )
                findNavController().navigate(R.id.action_guideListFragment_to_mainFragment)
            }
            if (imageList.isEmpty()) {
                MessageUtils.showNullErrorMessage(binding.tvErrorMessage, "생성한 가이드라인이 없습니다.")
            }
        } else {
            // 'recent' 타입일 때 API에서 이미지를 가져옴
            guideAdapter = GuideAdapter(emptyList(), R.layout.item_gallery_image) { photoData ->
                guideOverlayViewModel.setGuideImages(
                    photoData.originalImageUrl,
                    photoData.personGuidelineUrl,
                    photoData.backgroundGuidelineUrl
                )
                findNavController().navigate(R.id.action_guideListFragment_to_mainFragment)
                postUsageData(photoData.photoId ?: "")
            }
            getRecentGuideList()  // API 호출
        }

        binding.verticalRecyclerView.adapter = guideAdapter
        binding.verticalRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = guideAdapter
            setHasFixedSize(true)
        }

        binding.btnRetry.setOnClickListener{ getRecentGuideList() }
    }


    private fun getGuideListFromFolders(): List<UnifiedPhotoData> {
        val imageList = mutableListOf<UnifiedPhotoData>()
        val rootDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyApp/GuideImages")

        rootDir.listFiles()?.sortedByDescending { it.lastModified() }?.forEach { folder ->
            if (folder.isDirectory) {
                val originalFile = File(folder, "originalImage.png")
                val personFile = File(folder, "personGuideImage.png")
                val backgroundFile = File(folder, "backgroundGuideImage.png")

                // 모든 파일이 존재하는 경우에만 리스트에 추가
                if (originalFile.exists() && personFile.exists() && backgroundFile.exists()) {
                    imageList.add(
                        UnifiedPhotoData(
                            photoId = null, // 로컬 파일이므로 photoId는 null
                            originalImageUrl = originalFile.absolutePath,
                            personGuidelineUrl = personFile.absolutePath,
                            backgroundGuidelineUrl = backgroundFile.absolutePath
                        )
                    )
                }
            }
        }
        return imageList
    }

    private fun getRecentGuideList() {
        binding.tvErrorMessage.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
        deviceId = DeviceUtils.getDeviceId(requireContext())

        val call = RetrofitInstance.guideRecentService.getRecentUsagePhotos(deviceId)
        call.enqueue(object : Callback<RecentUsageResponse> {
            override fun onResponse(call: Call<RecentUsageResponse>, response: Response<RecentUsageResponse>) {
                if (response.isSuccessful) {
                    val recentData = response.body()?.data?.map { data ->
                        UnifiedPhotoData(
                            photoId = data.photoId,
                            originalImageUrl = data.maskImageUrl,
                            personGuidelineUrl = data.personGuidelineUrl,
                            backgroundGuidelineUrl = data.backgroundGuidelineUrl
                        )
                    } ?: emptyList()
                    if (recentData.isNotEmpty()) {
                        guideAdapter.updateData(recentData)
                    } else {
                        MessageUtils.showNullErrorMessage(binding.tvErrorMessage, "사용한 가이드라인이 없습니다.")
                    }
                } else if (response.code() == 404) {
                    // 404 에러일 때 처리
                    MessageUtils.showNullErrorMessage(binding.tvErrorMessage, "사용한 가이드라인이 없습니다.")
                }  else {
                    Log.d("API ERROR", "ERROR: ${response.body()}")
                    MessageUtils.showNetworkErrorMessage(binding.tvErrorMessage, binding.btnRetry)
                }
            }

            override fun onFailure(call: Call<RecentUsageResponse>, t: Throwable) {
                Log.d("API RESPONSE okhttp", "why here? ${t.message}")
                // 네트워크 오류 시 처리할 코드
                MessageUtils.showNetworkErrorMessage(binding.tvErrorMessage, binding.btnRetry)
            }
        })
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