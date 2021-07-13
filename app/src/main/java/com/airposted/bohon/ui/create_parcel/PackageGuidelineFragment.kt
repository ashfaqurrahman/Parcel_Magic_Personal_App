package com.airposted.bohon.ui.create_parcel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.airposted.bohon.databinding.FragmentPackageGuidelineBinding
import com.airposted.bohon.ui.adapter.TabAdapter
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

        binding.gotIt.setOnClickListener {
            requireActivity().onBackPressed()
        }
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

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {

    }
}