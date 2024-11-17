package com.example.mootd.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mootd.R
import com.example.mootd.adapter.GalleryAdapter
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.adapter.UnifiedPhotoData

class MapClusterGalleryFragment : Fragment() {

    private lateinit var galleryRecyclerView: RecyclerView
    private lateinit var guideAdapter: GuideAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_cluster_gallery_view, container, false)

        galleryRecyclerView = view.findViewById(R.id.recyclerView)
        galleryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        val photoDataList = arguments?.getSerializable("photoData") as? ArrayList<Map<String, String>>

        if (!photoDataList.isNullOrEmpty()) {
            val unifiedPhotoDataList = photoDataList.map { photoData ->
                UnifiedPhotoData(
                    photoId = photoData["photoId"],
                    originalImageUrl = photoData["originalImageUrl"] ?: "",
                    personGuidelineUrl = photoData["personGuidelineUrl"],
                    backgroundGuidelineUrl = photoData["backgroundGuidelineUrl"]
                )
            }

            guideAdapter = GuideAdapter(unifiedPhotoDataList, R.layout.item_gallery_image) { photoData ->
                // 클릭 이벤트 처리
                val bundle = Bundle().apply {
                    putString("photoId", photoData.photoId)
                    putString("originalImageUrl", photoData.originalImageUrl)
                    putString("personGuidelineUrl", photoData.personGuidelineUrl)
                    putString("backgroundGuidelineUrl", photoData.backgroundGuidelineUrl)
                }
                findNavController().navigate(R.id.action_mapClusterGalleryFragment_to_guideDetailFragment, bundle)
            }
            galleryRecyclerView.adapter = guideAdapter
        } else {
            Log.e("MapClusterGalleryFragment", "No photo data available")
            Toast.makeText(requireContext(), "이미지 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    companion object {
        fun newInstance(
            photoUrlsWithId: List<Pair<String?, String>>,
            photoUrlsWithoutId: List<String>
        ): MapClusterGalleryFragment {
            val fragment = MapClusterGalleryFragment()
            val args = Bundle()
            args.putSerializable("photoUrlsWithId", ArrayList(photoUrlsWithId))
            args.putStringArrayList("photoUrlsWithoutId", ArrayList(photoUrlsWithoutId))
            fragment.arguments = args
            return fragment
        }
    }
}
