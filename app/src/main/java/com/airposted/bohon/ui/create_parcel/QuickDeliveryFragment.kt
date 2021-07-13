package com.airposted.bohon.ui.create_parcel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bohon.databinding.FragmentQuickDeliveryBinding
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface

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

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            requireFragmentManager().beginTransaction().detach(this).attach(this).commit()
        }
    }
}