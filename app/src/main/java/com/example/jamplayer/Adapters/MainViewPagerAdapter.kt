package com.example.jamplayer.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MainViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
  private var  fragmentsList : ArrayList<Fragment> = ArrayList()
  private var titlesList : ArrayList<String> = ArrayList()
    override fun getCount(): Int {
return fragmentsList.size
    }

    override fun getItem(p0: Int): Fragment {
       return fragmentsList.get(p0)

    }
    fun addFragment (fragment: Fragment, title: String){
        fragmentsList.add(fragment)
        titlesList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titlesList.get(position)
    }
}