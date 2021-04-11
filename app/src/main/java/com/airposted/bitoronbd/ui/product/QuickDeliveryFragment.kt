package com.airposted.bitoronbd.ui.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bitoronbd.databinding.FragmentQuickDeliveryBinding
import com.airposted.bitoronbd.ui.home.HomeFragment
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface

class QuickDeliveryFragment : Fragment() {
    private lateinit var binding: FragmentQuickDeliveryBinding
    private var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuickDeliveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.gotIt.setOnClickListener {
            communicatorFragmentInterface!!.addContentFragment(HomeFragment(), false)
        }
    }
}