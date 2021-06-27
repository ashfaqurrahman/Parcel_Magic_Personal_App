package com.airposted.bohon.ui.product

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
    var quantity = 1
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
                binding.deliveryIcon.setBackgroundResource(R.drawable.ic_quick_large_icon)
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
        val close = dialogs.findViewById<ImageView>(R.id.cancel)
        val quantitySpinner = dialogs.findViewById<PowerSpinnerView>(R.id.quantity_spinner)
        quantitySpinner.setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            run {
                if (oldIndex != newIndex) {
                    quantity = newIndex + 1
                    //Toast.makeText(requireContext(), quantity.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        })
        done.setOnClickListener {
            dialogs.dismiss()
            val fragment = ReceiverInfoFragment()
            val bundle = Bundle()
            bundle.putInt("parcel_type", i)
            bundle.putInt("parcel_quantity", quantity)
            bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
            fragment.arguments = bundle
            communicatorFragmentInterface?.addContentFragment(fragment, true)
        }
        close.setOnClickListener {
            dialogs.dismiss()
        }
        dialogs.setCancelable(true)
        dialogs.show()
    }
}