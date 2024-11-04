package com.example.mootd.fragment

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.databinding.FragmentGuideImageListBinding
import java.io.File


class GuideImageListFragment : Fragment() {

    private var _binding: FragmentGuideImageListBinding? = null
    private val binding get() = _binding!!

    private lateinit var guideAdapter: GuideAdapter

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

        val imageList = if (listType == "new") {
            getGuideListFromFolders()
        } else {
            getRecentGuideList()
        }

        guideAdapter = GuideAdapter(
            imageList,
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

    private fun getGuideListFromFolders(): List<String> {
        val imageList = mutableListOf<String>()
        val rootDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyApp/GuideImages")

        rootDir.listFiles()?.sortedByDescending { it.lastModified() }?.forEach { folder ->
            if (folder.isDirectory) {
                val originalFile = File(folder, "original.png")
                if (originalFile.exists()) {
                    imageList.add(originalFile.absolutePath)
                }
            }
        }
        return imageList
    }

    private fun getRecentGuideList(): List<String> {
        return listOf(
            // 최근 사용 이미지 경로 추가
            "https://play-lh.googleusercontent.com/ecAdZGRwbLxhEENJauE6hMizUdpGaDL3BqAhib9cVeYmTMJLSe6XbbykbCTY_SCXbva0k8kixbcP2FfSQxjh",
            "https://play-lh.googleusercontent.com/ecAdZGRwbLxhEENJauE6hMizUdpGaDL3BqAhib9cVeYmTMJLSe6XbbykbCTY_SCXbva0k8kixbcP2FfSQxjh"
        )
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