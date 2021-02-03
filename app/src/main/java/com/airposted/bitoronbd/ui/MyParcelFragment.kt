package com.airposted.bitoronbd.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bitoronbd.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyParcelFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_parcel, container, false)

        val bottom = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottom.menu.getItem(1).isChecked = true

        return view
    }
    
}