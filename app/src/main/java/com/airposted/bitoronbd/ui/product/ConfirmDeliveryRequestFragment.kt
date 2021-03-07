package com.airposted.bitoronbd.ui.product

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentConfirmDeliveryRequestBinding
import com.airposted.bitoronbd.utils.round
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein

class ConfirmDeliveryRequestFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var binding: FragmentConfirmDeliveryRequestBinding
    private var finalCost = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfirmDeliveryRequestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {

        val deliveryType = arrayOf(
            "Quick",
            "Express"
        )

        val adapter =
            ArrayAdapter(requireActivity(), R.layout.row, R.id.text_view_name, deliveryType)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                finalCost = calculatePrice(position)
                binding.charge.text = finalCost
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.toolbarTitle.text = getString(R.string.one_final_step)

        binding.name.text = requireArguments().getString("name")
        binding.phone.text = requireArguments().getString("phone")

        binding.from.text = requireArguments().getString("location_name")
        binding.to.text = PreferenceProvider(
            requireActivity()
        ).getSharedPreferences("currentLocation")
        binding.distance.text = distance()

        binding.confirmDeliveryRequest.setOnClickListener {
            if (finalCost != "") {
                val dialogs = Dialog(requireActivity())
                dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogs.setContentView(R.layout.successful_dialog)
                dialogs.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogs.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,  //w
                    ViewGroup.LayoutParams.WRAP_CONTENT //h
                )

                val radioButtonID = binding.radioGroup.checkedRadioButtonId
                val radioButton: RadioButton = binding.radioGroup.findViewById(radioButtonID)
                val selectedtext = radioButton.text

                val body = dialogs.findViewById<TextView>(R.id.body)
                val proceed = dialogs.findViewById<TextView>(R.id.proceed)
                body.text = getString(R.string.successful_order_text, "#12345")

                if (selectedtext == "Receiver") {
                    proceed.text = getString(R.string.view_this_parcel)
                } else {
                    proceed.text = getString(R.string.proceed_to_payment)
                }

                proceed.setOnClickListener {
                    dialogs.dismiss()
                    when(selectedtext){
                        "Receiver" -> {

                        }
                        "Sender" -> {

                        }
                    }
                }

                dialogs.setCancelable(false)

                dialogs.show()
            }
        }
    }

    private fun distance(): String {
        return round(requireArguments().getFloat("distance").toDouble(), 2).toString() + " Km"
    }

    private fun calculatePrice(position: Int): String {
        return when (position) {
            0 -> "৳" + round(
                ((requireArguments().getFloat("distance") * PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("rate_quick")!!.toFloat()) + PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("base_price_personal")!!.toFloat()).toDouble(), 2
            ).toString()
            else -> "৳" + round(
                ((requireArguments().getFloat("distance") * PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("rate_express")!!.toFloat()) + PreferenceProvider(
                    requireActivity()
                ).getSharedPreferences("base_price_personal")!!.toFloat()).toDouble(), 2
            ).toString()
        }
    }

}

