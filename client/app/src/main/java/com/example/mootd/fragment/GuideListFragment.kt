package com.example.mootd.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mootd.R
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.adapter.GuideListPagerAdapter
import com.example.mootd.databinding.FragmentGuideListBinding
import com.google.android.material.tabs.TabLayoutMediator


class GuideListFragment : Fragment() {
    private var _binding: FragmentGuideListBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentGuideListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닫기 버튼 설정
        binding.btnClose.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnClose.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // ViewPager와 Adapter 설정
        val pagerAdapter = GuideListPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // TabLayout과 ViewPager 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "신규 항목"
                else -> "최근 사용"
            }
        }.attach()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}