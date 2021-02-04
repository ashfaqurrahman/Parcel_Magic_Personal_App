package com.airposted.bitoronbd.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentSettingBinding
import com.airposted.bitoronbd.utils.Coroutines
import com.bumptech.glide.Glide
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SettingFragment : Fragment(), KodeinAware {
    private lateinit var settingBinding: FragmentSettingBinding
    override val kodein by kodein()

    private val factory: SettingViewModelFactory by instance()
    private lateinit var viewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingBinding = FragmentSettingBinding.inflate(inflater, container, false)

        return settingBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(SettingViewModel::class.java)
        bindUI()
    }

    private fun bindUI() = Coroutines.main {
        viewModel.getName.await().observe(requireActivity(), {
            settingBinding.profileName.text = it
        })
        viewModel.getImage.await().observe(requireActivity(), {
            Glide.with(requireActivity()).load(it).placeholder(R.drawable.sample_pro_pic).error(
                R.drawable.sample_pro_pic
            ).into(settingBinding.profileImage)
        })

        settingBinding.myParcel.setOnClickListener  {
            //click()
        }
        settingBinding.profile.setOnClickListener  {
            Navigation.findNavController(requireView()).navigate(
                R.id.profileFragment)
        }
    }
}