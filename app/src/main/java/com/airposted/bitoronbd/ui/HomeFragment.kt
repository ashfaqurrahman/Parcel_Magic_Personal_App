package com.airposted.bitoronbd.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.airposted.bitoronbd.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val bottom = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottom.menu.getItem(0).isChecked = true

        return view
    }*/
}