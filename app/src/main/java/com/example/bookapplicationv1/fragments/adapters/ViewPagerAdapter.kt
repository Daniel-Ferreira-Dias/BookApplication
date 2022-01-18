package com.example.bookapplicationv1.fragments.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bookapplicationv1.fragments.HomeFragment
import com.example.bookapplicationv1.fragments.MessageFragment
import com.example.bookapplicationv1.fragments.ProfileFragment
import com.example.bookapplicationv1.fragments.SearchFragment

class ViewPagerAdapter(activity: FragmentActivity, private val tabCount : Int) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return tabCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> HomeFragment()
            1 -> SearchFragment()
            2 -> MessageFragment()
            3 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}