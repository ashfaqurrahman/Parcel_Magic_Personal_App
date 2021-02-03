package com.airposted.bitoronbd.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentSettingBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingFragment : Fragment() {
    private lateinit var settingBinding: FragmentSettingBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingBinding = FragmentSettingBinding.inflate(inflater, container, false)
        val view = settingBinding.root

        Glide.with(requireActivity()).load(
            PersistentUser.getInstance().getUserImage(requireActivity())
        ).placeholder(R.drawable.sample_pro_pic).error(
            R.drawable.sample_pro_pic
        ).into(settingBinding.profileImage)

        val bottom = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottom.menu.getItem(2).isChecked = true

        settingBinding.profileName.text = PersistentUser.getInstance().getFullName(requireActivity())

        return view
    }
}