package com.airposted.bohon.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.airposted.bohon.ui.create_parcel.ExpressDeliveryFragment
import com.airposted.bohon.ui.create_parcel.QuickDeliveryFragment

class TabAdapter(fm: FragmentManager, private var totalTabs: Int) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> {
                fragment = ExpressDeliveryFragment()
            }
            1 -> {
                fragment = QuickDeliveryFragment()
            }
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return totalTabs
    }
}