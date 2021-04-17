package com.airposted.bitoronbd.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airposted.bitoronbd.databinding.FragmentReceiverInfoBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
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
            if(binding.receiverName.text.isNotEmpty()){
                val fragment = ReceiverAddressFragment()
                val bundle = Bundle()
                bundle.putString("receiver_name", binding.receiverName.text.toString())
                bundle.putString("receiver_phone", binding.receiverPhone.text.toString())
                bundle.putString("delivery_type", requireArguments().getString("delivery_type"))
                bundle.putString("parcel_type", requireArguments().getString("parcel_type"))
                fragment.arguments = bundle
                communicatorFragmentInterface!!.addContentFragment(fragment, true)
            } else {
                binding.rootLayout.snackbar("Please enter receiver name")
            }
        }

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        textWatcher(requireActivity(), 9, binding.receiverPhone, binding.next)
    }
}