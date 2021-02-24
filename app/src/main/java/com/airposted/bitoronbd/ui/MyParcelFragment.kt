package com.airposted.bitoronbd.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.airposted.bitoronbd.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyParcelFragment : Fragment(R.layout.fragment_my_parcel) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).menu.getItem(1).isChecked = true
    }

}