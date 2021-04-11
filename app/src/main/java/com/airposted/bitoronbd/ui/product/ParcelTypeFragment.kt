package com.airposted.bitoronbd.ui.product

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentParcelTypeBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface

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
            requireActivity().onBackPressed()
        }
        when(requireArguments().getString("delivery_type")) {
            "express" -> {
                binding.deliveryType.text = getString(R.string.express)
                binding.deliveryIcon.setBackgroundResource(R.drawable.ic_express_icon)
            }
            "quick" -> {
                binding.deliveryType.text = getString(R.string.quick)
                binding.deliveryIcon.setBackgroundResource(R.drawable.ic_quick_icon)
            }
        }


        binding.fragileBtn.setOnClickListener {
            val fragment = ReceiverInfoFragment()
            val bundle = Bundle()
            bundle.putString("parcel_type", "fragile")
            bundle.putString("delivery_type", requireArguments().getString("delivery_type"))
            fragment.arguments = bundle
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }

        binding.liquidBtn.setOnClickListener {
            val fragment = ReceiverInfoFragment()
            val bundle = Bundle()
            bundle.putString("parcel_type", "liquid")
            bundle.putString("delivery_type", requireArguments().getString("delivery_type"))
            fragment.arguments = bundle
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }

        binding.docsBtn.setOnClickListener {
            val fragment = ReceiverInfoFragment()
            val bundle = Bundle()
            bundle.putString("parcel_type", "document")
            bundle.putString("delivery_type", requireArguments().getString("delivery_type"))
            fragment.arguments = bundle
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }

        binding.solidBtn.setOnClickListener {
            val fragment = ReceiverInfoFragment()
            val bundle = Bundle()
            bundle.putString("parcel_type", "solid")
            bundle.putString("delivery_type", requireArguments().getString("delivery_type"))
            fragment.arguments = bundle
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                ContextCompat.getColor(requireActivity(), R.color.color1),
                ContextCompat.getColor(requireActivity(), R.color.color2),
                ContextCompat.getColor(requireActivity(), R.color.color3),
                ContextCompat.getColor(requireActivity(), R.color.color4)
            )
        )

        binding.fragileBtn.background = gradientDrawable
        binding.liquidBtn.background = gradientDrawable
        binding.docsBtn.background = gradientDrawable
        binding.solidBtn.background = gradientDrawable
    }
}