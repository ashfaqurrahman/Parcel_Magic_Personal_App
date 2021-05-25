package com.airposted.bitoronbd.ui.product

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentParcelTypeBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.hideKeyboard
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
                binding.deliveryIcon.setBackgroundResource(R.drawable.ic_quick_icon)
            }
        }

        binding.envelopeBtn.setOnClickListener {
            showQuantityDialog(3)
        }

        binding.smallBtn.setOnClickListener {
            showQuantityDialog(1)
        }

        binding.largeBtn.setOnClickListener {
            showQuantityDialog(2)
        }
    }

    private fun showQuantityDialog(i: Int) {
        val dialogs = Dialog(requireActivity())
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(R.layout.quantity_dialog)
        dialogs.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogs.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,  //w
            ViewGroup.LayoutParams.MATCH_PARENT //h
        )
        val done = dialogs.findViewById<TextView>(R.id.done)
        val quantitySpinner = dialogs.findViewById<PowerSpinnerView>(R.id.quantity_spinner)
        var quantity = "01"
        quantitySpinner.setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            run {
                if (oldIndex != newIndex) {
                    Toast.makeText(requireContext(), newItem, Toast.LENGTH_SHORT).show()
                    quantity = newItem
                }
            }
        })
        done.setOnClickListener {
            dialogs.dismiss()
            val fragment = ReceiverInfoFragment()
            val bundle = Bundle()
            bundle.putInt("parcel_type", i)
            bundle.putString("parcel_quantity", quantity)
            bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
            fragment.arguments = bundle
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }
        dialogs.setCancelable(true)
        dialogs.show()
    }
}