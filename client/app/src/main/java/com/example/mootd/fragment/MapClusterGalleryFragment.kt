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

class MapClusterGalleryFragment : Fragment() {

    private lateinit var galleryRecyclerView: RecyclerView
    private lateinit var galleryAdapter: GalleryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_cluster_gallery_view, container, false)

        galleryRecyclerView = view.findViewById(R.id.recyclerView)
        galleryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        val photoUrlsWithId: List<Pair<String?, String>>? =
            arguments?.getSerializable("photoUrlsWithId") as? List<Pair<String?, String>>
        val photoUrlsWithoutId: List<String>? =
            arguments?.getStringArrayList("photoUrlsWithoutId")

        Log.d("MapClusterGalleryFragment", "photoUrlsWithId: $photoUrlsWithId, photoUrlsWithoutId: $photoUrlsWithoutId")

        galleryAdapter = when {
            // photoUrlsWithId가 null이 아니면
            photoUrlsWithId != null -> {
                GalleryAdapter(photoUrlsWithId) { imageId, imageUri ->
                    // 이미지를 클릭했을 때 Bundle에 데이터 담아 GuideDetailFragment로 이동
                    val bundle = Bundle().apply {
                        putString("photoId", imageId)
                        putString("imageUrl", imageUri)
                    }
                    findNavController().navigate(R.id.action_mapClusterGalleryFragment_to_guideDetailFragment, bundle)
                }
            }
            // photoUrlsWithoutId가 null이 아니면
            photoUrlsWithoutId != null -> {
                GalleryAdapter(photoUrlsWithoutId) { imageUri ->
                    // 사진 ID가 없는 경우에도 클릭 이벤트 처리
                    val bundle = Bundle().apply {
                        putString("imageUrl", imageUri)
                    }
                    findNavController().navigate(R.id.action_mapClusterGalleryFragment_to_guideDetailFragment, bundle)
                }
            }
            else -> {
                Toast.makeText(requireContext(), "이미지 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                return view // 이미지 데이터가 없으면 리턴
            }
        }

        galleryRecyclerView.adapter = galleryAdapter
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
