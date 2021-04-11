package com.airposted.bitoronbd.ui.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentPackageGuidelineBinding
import com.airposted.bitoronbd.ui.adapter.TabAdapter
import com.google.android.material.tabs.TabLayout

class PackageGuidelineFragment : Fragment() {
    private lateinit var binding: FragmentPackageGuidelineBinding
    var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPackageGuidelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        tabLayout = binding.tabs
        viewPager = binding.pager
        val adapter = TabAdapter(requireFragmentManager(), tabLayout!!.tabCount)
        viewPager!!.adapter = adapter
        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }
}