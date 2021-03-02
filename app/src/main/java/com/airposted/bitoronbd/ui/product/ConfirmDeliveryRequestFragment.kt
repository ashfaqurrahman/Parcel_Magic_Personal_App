package com.airposted.bitoronbd.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentConfirmDeliveryRequestBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.round
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import java.util.*

class ConfirmDeliveryRequestFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var binding: FragmentConfirmDeliveryRequestBinding
    private var mDeliveryList: ArrayList<DeliveryItem>? = null
    private var mAdapter: DeliveryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentConfirmDeliveryRequestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        /*mDeliveryList = ArrayList()
        mDeliveryList!!.add(DeliveryItem("Express", R.drawable.ic_car))
        mDeliveryList!!.add(DeliveryItem("Quick", R.drawable.ic_car))
        mAdapter = DeliveryAdapter(requireActivity(), mDeliveryList)
        binding.spinner.adapter = mAdapter
        binding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val clickedItem: DeliveryItem = parent.getItemAtPosition(position) as DeliveryItem
                val clickedCountryName: String = clickedItem.countryName
                Toast.makeText(
                    requireActivity(),
                    "$clickedCountryName selected",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }*/

        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.toolbarTitle.text = "One Final Step"

        binding.name.text = requireArguments().getString("name")
        binding.phone.text = requireArguments().getString("phone")

        binding.from.text =  requireArguments().getString("location_name")
        binding.to.text = PreferenceProvider(
                requireActivity()
                ).getSharedPreferences("currentLocation")
        binding.distance.text = round(requireArguments().getFloat("distance").toDouble(), 2).toString() + " Km"
        binding.charge.text = "৳" + round((requireArguments().getFloat("distance") * PreferenceProvider(requireActivity()).getSharedPreferences("rate")!!.toFloat()).toDouble(), 2).toString()
    }

}

