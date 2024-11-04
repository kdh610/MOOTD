package com.example.mootd.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mootd.fragment.GuideImageListFragment

class GuideListPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GuideImageListFragment.newInstance("new")  // 신규 항목 프래그먼트
            else -> GuideImageListFragment.newInstance("recent")  // 최근 사용 프래그먼트
        }
    }
}
