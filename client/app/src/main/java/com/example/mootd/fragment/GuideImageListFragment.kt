package com.example.mootd.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.databinding.FragmentGuideImageListBinding


class GuideImageListFragment : Fragment() {

    private var _binding: FragmentGuideImageListBinding? = null
    private val binding get() = _binding!!

    private lateinit var guideAdapter: GuideAdapter

    private val guideImageList = listOf(
        // 이미지 경로 추가
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg"

    )

    private val recentImageList = listOf(
        // 최근 사용 이미지 경로 추가
        "https://play-lh.googleusercontent.com/ecAdZGRwbLxhEENJauE6hMizUdpGaDL3BqAhib9cVeYmTMJLSe6XbbykbCTY_SCXbva0k8kixbcP2FfSQxjh",
        "https://play-lh.googleusercontent.com/ecAdZGRwbLxhEENJauE6hMizUdpGaDL3BqAhib9cVeYmTMJLSe6XbbykbCTY_SCXbva0k8kixbcP2FfSQxjh"
    )

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
        guideAdapter = GuideAdapter(
            if (listType == "new") guideImageList else recentImageList,
            R.layout.item_gallery_image
        ) { imageUri ->
            println("Click: ${imageUri}")
        }

        binding.verticalRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = guideAdapter
            setHasFixedSize(true)
        }
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