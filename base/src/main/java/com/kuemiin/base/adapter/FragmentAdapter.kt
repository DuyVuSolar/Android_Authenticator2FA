package com.kuemiin.base.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

class FragmentAdapter constructor(fm: FragmentManager, var mFragments: MutableList<Fragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }

    override fun getCount(): Int {
        return mFragments.size
    }
//
    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
//            PagerAdapter.POSITION_NONE
//            super.getItemPosition(`object`)
    }
}

class FragmentAdapterState(fm: FragmentManager, var mFragments: MutableList<Fragment>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }

    override fun getCount(): Int {
        return mFragments.size
    }
//
    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
//            PagerAdapter.POSITION_NONE
//            super.getItemPosition(`object`)
    }
}
