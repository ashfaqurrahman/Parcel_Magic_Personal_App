package com.airposted.bohon.ui.create_parcel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bohon.databinding.FragmentExpressDeliveryBinding
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface

class ExpressDeliveryFragment : Fragment() {
    private lateinit var binding: FragmentExpressDeliveryBinding
    private var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpressDeliveryBinding.inflate(inflater, container, false)
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