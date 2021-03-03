package com.airposted.bitoronbd.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentConfirmDeliveryRequestBinding
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.round
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
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
        binding.spinnerView.apply {
            setSpinnerAdapter(IconSpinnerAdapter(this))
            setItems(
                arrayListOf(
                    IconSpinnerItem(iconRes = R.drawable.ic_car, text = "Express"),
                    IconSpinnerItem(iconRes = R.drawable.ic_car, text = "Quick")
                )
            )
            setOnSpinnerItemSelectedListener<IconSpinnerItem> { _, _, _, item ->
                Toast.makeText(requireContext(), item.text, Toast.LENGTH_SHORT).show()
            }
            getSpinnerRecyclerView().layoutManager = GridLayoutManager(requireContext(), 1)
            selectItemByIndex(0)
        }

        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.toolbarTitle.text = "One Final Step"

//        binding.name.text = requireArguments().getString("name")
//        binding.phone.text = requireArguments().getString("phone")
//
//        binding.from.text =  requireArguments().getString("location_name")
//        binding.to.text = PreferenceProvider(
//                requireActivity()
//                ).getSharedPreferences("currentLocation")
//        binding.distance.text = round(requireArguments().getFloat("distance").toDouble(), 2).toString() + " Km"
//        binding.charge.text = "৳" + round((requireArguments().getFloat("distance") * PreferenceProvider(requireActivity()).getSharedPreferences("rate")!!.toFloat()).toDouble(), 2).toString()
    }

}

