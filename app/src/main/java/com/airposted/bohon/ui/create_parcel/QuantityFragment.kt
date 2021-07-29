package com.airposted.bohon.ui.create_parcel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airposted.bohon.R
import com.airposted.bohon.databinding.FragmentParcelTypeBinding
import com.airposted.bohon.databinding.FragmentQuantityBinding
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface
import com.airposted.bohon.utils.hideKeyboard
import com.airposted.bohon.utils.snackbar

class QuantityFragment : Fragment() {
    private lateinit var binding: FragmentQuantityBinding
    private var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    var quantity = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuantityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.toolbarTitle.text = "Parcel Quantity"

        binding.increment.setOnClickListener {
            if (binding.quantity.text.toString() != "05") {
                quantity += 1
                binding.quantity.text = "0$quantity"
            } else {
                binding.rootLayout.snackbar("Quantity at most 5")
            }
        }

        binding.decrement.setOnClickListener {
            if (binding.quantity.text.toString() == "01") {
                binding.rootLayout.snackbar("Quantity at least 1")
            } else {
                quantity -= 1
                binding.quantity.text = "0$quantity"
            }
        }

        binding.done.setOnClickListener {
            val fragment = ReceiverInfoFragment()
            val bundle = Bundle()
            bundle.putInt("parcel_type", requireArguments().getInt("parcel_type"))
            bundle.putInt("parcel_quantity", quantity)
            bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
            fragment.arguments = bundle
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }
    }

}