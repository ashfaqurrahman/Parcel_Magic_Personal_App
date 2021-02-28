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
import com.airposted.bitoronbd.databinding.FragmentConfirmDeliveryRequestBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
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
        mDeliveryList = ArrayList()
        mDeliveryList!!.add(DeliveryItem("Express", R.drawable.ic_car))
        mDeliveryList!!.add(DeliveryItem("Quick", R.drawable.ic_car))
        mAdapter = DeliveryAdapter(requireContext(), mDeliveryList)
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
                    requireContext(),
                    "$clickedCountryName selected",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

}

