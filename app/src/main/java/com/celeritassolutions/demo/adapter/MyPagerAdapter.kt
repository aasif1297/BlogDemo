package com.celeritassolutions.demo.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.celeritassolutions.demo.fragments.MyAccount
import com.celeritassolutions.demo.fragments.MyInformation

class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                MyAccount()
            }
            else -> {
                return MyInformation()
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "MY ACCOUNT"
            else -> {
                return "MY INFORMATION"
            }
        }
    }
}