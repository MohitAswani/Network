package com.example.network.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.network.R
import com.example.network.recent.Recent
import com.example.network.status.Status

class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val titles= arrayOf(R.string.tab_chat,R.string.tab_status)
    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> Recent()
            1-> Status()
            else->Recent()
        }
    }

}