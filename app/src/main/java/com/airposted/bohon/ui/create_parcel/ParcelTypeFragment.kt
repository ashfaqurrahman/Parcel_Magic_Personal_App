package com.airposted.bohon.ui.create_parcel

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.airposted.bohon.R
import com.airposted.bohon.databinding.FragmentParcelTypeBinding
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface
import com.airposted.bohon.utils.hideKeyboard
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerView

class ParcelTypeFragment : Fragment() {
    private lateinit var binding: FragmentParcelTypeBinding
    private var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParcelTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()

    }

    private fun bindUI() {
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.back.setOnClickListener {
            hideKeyboard(requireActivity())
            requireActivity().onBackPressed()
        }
        when(requireArguments().getInt("delivery_type")) {
            2 -> {
                binding.deliveryType.text = getString(R.string.express)
                binding.deliveryIcon.setBackgroundResource(R.drawable.ic_express_icon)
            }
            1 -> {
                binding.deliveryType.text = getString(R.string.quick)
                binding.deliveryIcon.setBackgroundResource(R.drawable.ic_regular_icon)
            }
        }

        binding.envelopeBtn.setOnClickListener {
            goQuantityPage(1)
        }

        binding.smallBtn.setOnClickListener {
            goQuantityPage(2)
        }

        binding.largeBtn.setOnClickListener {
            goQuantityPage(3)
        }
    }

    private fun goQuantityPage(i: Int) {
        val fragment = QuantityFragment()
        val bundle = Bundle()
        bundle.putInt("parcel_type", i)
        bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
        fragment.arguments = bundle
        communicatorFragmentInterface?.addContentFragment(fragment, true)
    }
}