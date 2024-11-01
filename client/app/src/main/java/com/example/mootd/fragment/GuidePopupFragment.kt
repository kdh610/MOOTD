package com.example.mootd.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.databinding.FragmentGuidePopupBinding

class GuidePopupFragment : DialogFragment() {
    private var _binding: FragmentGuidePopupBinding? = null
    private val binding get() = _binding!!

    private val guideImageList = listOf(
        // 이미지 경로 추가
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg",
        "https://i.pinimg.com/736x/d9/16/44/d9164496ef8a969477fe3c698694ecc5.jpg"

    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuidePopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닫기 버튼 리스너
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 세로 스크롤 RecyclerView 설정
        binding.verticalRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = GuideAdapter(guideImageList) { imageUri ->
                // 클릭 이벤트 처리
                println("Image clicked: $imageUri")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}