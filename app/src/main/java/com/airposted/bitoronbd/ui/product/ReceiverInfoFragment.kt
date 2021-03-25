package com.airposted.bitoronbd.ui.product

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentReceiverInfoBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface

class ReceiverInfoFragment : Fragment() {
    private lateinit var binding: FragmentReceiverInfoBinding
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReceiverInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.next.setOnClickListener {
            communicatorFragmentInterface!!.addContentFragment(ReceiverAddressFragment(), true)
        }
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                ContextCompat.getColor(requireActivity(), R.color.color1),
                ContextCompat.getColor(requireActivity(), R.color.color2),
                ContextCompat.getColor(requireActivity(), R.color.color3),
                ContextCompat.getColor(requireActivity(), R.color.color4)
            )
        )
        binding.back.background = gradientDrawable
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}