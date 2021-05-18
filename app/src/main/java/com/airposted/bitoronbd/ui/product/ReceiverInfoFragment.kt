package com.airposted.bitoronbd.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airposted.bitoronbd.databinding.FragmentReceiverInfoBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.hideKeyboard
import com.airposted.bitoronbd.utils.snackbar
import com.airposted.bitoronbd.utils.textWatcher

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
            hideKeyboard(requireActivity())
            if(binding.receiverName.text.isNotEmpty()){
                val phone = binding.receiverPhone.text.toString()
                val fragment = ReceiverAddressFragment()
                val bundle = Bundle()
                bundle.putString("receiver_name", binding.receiverName.text.toString())
                bundle.putString("receiver_phone", "+8801$phone")
                bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
                bundle.putInt("parcel_type", requireArguments().getInt("parcel_type"))
                fragment.arguments = bundle
                communicatorFragmentInterface!!.addContentFragment(fragment, true)
            } else {
                binding.rootLayout.snackbar("Please enter receiver name")
            }
        }

        binding.back.setOnClickListener {
            hideKeyboard(requireActivity())
            requireActivity().onBackPressed()
        }

        textWatcher(requireActivity(), 8, binding.receiverPhone, binding.next)
    }
}