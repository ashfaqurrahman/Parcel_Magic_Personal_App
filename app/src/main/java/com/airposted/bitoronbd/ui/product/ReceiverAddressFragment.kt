package com.airposted.bitoronbd.ui.product

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentReceiverAddressBinding
import com.airposted.bitoronbd.model.Prediction
import com.airposted.bitoronbd.model.SearchLocation
import com.airposted.bitoronbd.ui.location_set.CustomClickListener
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModel
import com.airposted.bitoronbd.ui.location_set.LocationSetViewModelFactory
import com.airposted.bitoronbd.ui.location_set.MyRecyclerViewAdapter
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.ApiException
import com.airposted.bitoronbd.utils.NoInternetException
import com.airposted.bitoronbd.utils.hideKeyboard
import com.airposted.bitoronbd.utils.snackbar
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import kotlin.properties.Delegates


class ReceiverAddressFragment : Fragment(), KodeinAware, CustomClickListener {
    private lateinit var binding: FragmentReceiverAddressBinding
    private lateinit var viewModel: LocationSetViewModel
    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var list: SearchLocation
    var myCommunicator: CommunicatorFragmentInterface? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReceiverAddressBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), factory).get(LocationSetViewModel::class.java)
        bindUI()
    }

    private fun bindUI() {

        myCommunicator = context as CommunicatorFragmentInterface

        binding.toolbar.toolbarTitle.text = "Where are you sending?"
        binding.toolbar.backImage.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.map.setOnClickListener {
            viewModel.setOnMapTrue()
            requireActivity().onBackPressed()
        }

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.search.isEnabled = false
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                location(newText)
                return false
            }
        })
    }

    private fun location(location: String) {
        if (location.length > 2) {
            val btnClose: ImageView = binding.search.findViewById(R.id.search_close_btn)
            btnClose.visibility = View.GONE
            binding.loading.visibility = View.VISIBLE
            binding.recyclerview.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val sb =
                        StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?")
                    sb.append("input=$location")
                    sb.append("&key=AIzaSyB8gzYgvsy-1TufBYLYaD58EYDTWUZBWZQ")
                    sb.append("&components=country:bd")
                    list = viewModel.getLocations(sb.toString())
                    binding.loading.visibility = View.GONE
                    btnClose.visibility = View.VISIBLE
                    if (list.predictions.isNotEmpty()) {
                        val myRecyclerViewAdapter = MyRecyclerViewAdapter(
                            list.predictions,
                            this@ReceiverAddressFragment,
                        )
                        binding.recyclerview.layoutManager = GridLayoutManager(
                            requireActivity(),
                            1
                        )
                        binding.recyclerview.itemAnimator = DefaultItemAnimator()
                        binding.recyclerview.adapter = myRecyclerViewAdapter
                    } else {
                        binding.recyclerview.visibility = View.GONE
                    }
                } catch (e: ApiException) {
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                }
            }
        } else {
            binding.recyclerview.visibility = View.GONE
        }
    }

    override fun onItemClick(location: Prediction) {
        hideKeyboard(requireActivity())
        binding.search.setQuery(location.description, false)
        binding.recyclerview.visibility = View.GONE
        binding.search.clearFocus()

        val fragment = ConfirmReceiverAddressFragment()
        val bundle = Bundle()
        bundle.putString("location_name", location.description)
        bundle.putDouble("latitude", getLatLngFromAddress(location.description)!!.latitude)
        bundle.putDouble("longitude", getLatLngFromAddress(location.description)!!.longitude)
        fragment.arguments = bundle
        myCommunicator?.addContentFragment(fragment, false)
    }

    private fun getLatLngFromAddress(address: String): LatLng? {
        val geocoder = Geocoder(requireActivity())
        val addressList: List<Address>?
        return try {
            addressList = geocoder.getFromLocationName(address, 1)
            if (addressList != null) {
                val singleAddress = addressList[0]
                LatLng(singleAddress.latitude, singleAddress.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}