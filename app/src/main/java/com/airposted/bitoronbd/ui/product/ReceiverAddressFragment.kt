package com.airposted.bitoronbd.ui.product

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentReceiverAddressBinding
import com.airposted.bitoronbd.model.SearchLocation
import com.airposted.bitoronbd.ui.location_set.*
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*


class ReceiverAddressFragment : Fragment(), KodeinAware, CustomClickListener {
    private lateinit var binding: FragmentReceiverAddressBinding
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var viewModel: LocationSetViewModel
    private lateinit var list: SearchLocation
    private var from = false

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
        communicatorFragmentInterface = context as CommunicatorFragmentInterface

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.map.setOnClickListener {
            communicatorFragmentInterface!!.addContentFragment(LocationSetFragment(), true)
        }

        if (PreferenceProvider(requireActivity()).getSharedPreferences("latitude") == null){
            val editText =
                binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            editText.requestFocus()
        }else {
            val geo = Geocoder(requireActivity(), Locale.getDefault())
            val addresses = geo.getFromLocation(PreferenceProvider(requireActivity()).getSharedPreferences("latitude")!!.toDouble(), PreferenceProvider(requireActivity()).getSharedPreferences("longitude")!!.toDouble(), 1);
            if (addresses.isNotEmpty()) {
                binding.searchFrom.setQuery(addresses[0].featureName + ", " + addresses[0].thoroughfare, false)
            }
            val editText =
                binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            editText.requestFocus()
        }

        binding.searchFrom.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.searchFrom.isEnabled = false
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                from = true
                locationFrom(newText)
                return false
            }
        })

        binding.searchTo.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.searchTo.isEnabled = false
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                locationTo(newText)
                return false
            }
        })
    }

    private fun locationFrom(location: String) {
        if (location.length > 2) {
            val btnClose: ImageView = binding.searchFrom.findViewById(R.id.search_close_btn)
            btnClose.visibility = View.GONE
            binding.loadingFrom.visibility = View.VISIBLE
            binding.recyclerview.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val sb =
                        StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?")
                    sb.append("input=$location")
                    sb.append("&key=AIzaSyB8gzYgvsy-1TufBYLYaD58EYDTWUZBWZQ")
                    sb.append("&components=country:bd")
                    list = viewModel.getLocations(sb.toString())
                    binding.loadingFrom.visibility = View.GONE
                    btnClose.visibility = View.VISIBLE
                    if (list.predictions.isNotEmpty()) {
                        val term = ArrayList<String> ()
                        for (i in list.predictions.indices){
                            if (list.predictions[i].terms.size > 1){
                                var text = ""
                                for (j in 0 until list.predictions[i].terms.size - 1){
                                    text += if (j > 0){
                                        ", " + list.predictions[i].terms[j].value
                                    } else {
                                        list.predictions[i].terms[j].value
                                    }
                                }
                                term.add(text)
                            } else {
                                var text = ""
                                for (j in list.predictions[i].terms.indices){
                                    text += if (j > 0){
                                        ", " + list.predictions[i].terms[j].value
                                    } else {
                                        list.predictions[i].terms[j].value
                                    }
                                }
                                term.add(text)
                            }
                        }

                        val myRecyclerViewAdapter = LocationSetRecyclerViewAdapter(
                            term,
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

    private fun locationTo(location: String) {
        if (location.length > 2) {
            val btnClose: ImageView = binding.searchTo.findViewById(R.id.search_close_btn)
            btnClose.visibility = View.GONE
            binding.loadingTo.visibility = View.VISIBLE
            binding.recyclerview.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val sb =
                        StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?")
                    sb.append("input=$location")
                    sb.append("&key=AIzaSyB8gzYgvsy-1TufBYLYaD58EYDTWUZBWZQ")
                    sb.append("&components=country:bd")
                    list = viewModel.getLocations(sb.toString())
                    binding.loadingTo.visibility = View.GONE
                    btnClose.visibility = View.VISIBLE
                    if (list.predictions.isNotEmpty()) {
                        val term = ArrayList<String> ()
                        for (i in list.predictions.indices){
                            if (list.predictions[i].terms.size > 1){
                                var text = ""
                                for (j in 0 until list.predictions[i].terms.size - 1){
                                    text += if (j > 0){
                                        ", " + list.predictions[i].terms[j].value
                                    } else {
                                        list.predictions[i].terms[j].value
                                    }
                                }
                                term.add(text)
                            } else {
                                var text = ""
                                for (j in list.predictions[i].terms.indices){
                                    text += if (j > 0){
                                        ", " + list.predictions[i].terms[j].value
                                    } else {
                                        list.predictions[i].terms[j].value
                                    }
                                }
                                term.add(text)
                            }
                        }

                        val myRecyclerViewAdapter = LocationSetRecyclerViewAdapter(
                            term,
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

    override fun onItemClick(location: String) {
        setProgressDialog(requireActivity())
        hideKeyboard(requireActivity())
        binding.recyclerview.visibility = View.GONE
        if (from){
            binding.searchFrom.setQuery(location, false)
            binding.searchFrom.clearFocus()
            if (binding.searchTo.query.isNotEmpty()){
                val fragment = ConfirmReceiverAddressFragment()
                val bundle = Bundle()
                val geo = Geocoder(requireActivity(), Locale.getDefault())
                val addresses = geo.getFromLocation(getLatLngFromAddress(location)!!.latitude, getLatLngFromAddress(location)!!.longitude, 1);
                if (addresses.isNotEmpty()) {
                    bundle.putString("city", addresses[0].locality)
                    if (addresses[0].subLocality != null){
                        bundle.putString("area", addresses[0].subLocality)
                        bundle.putString("city", addresses[0].subLocality)
                    } else {
                        bundle.putString("area", addresses[0].locality)
                        bundle.putString("city", addresses[0].locality)
                    }
                }
                bundle.putString("sender_location_name", binding.searchFrom.query.toString())
                bundle.putString("receiver_location_name", binding.searchTo.query.toString())
                bundle.putDouble("latitude", getLatLngFromAddress(location)!!.latitude)
                bundle.putDouble("longitude", getLatLngFromAddress(location)!!.longitude)
                bundle.putString("receiver_name", requireArguments().getString("receiver_name"))
                bundle.putString("receiver_phone", requireArguments().getString("receiver_phone"))
                fragment.arguments = bundle
                dismissDialog()
                communicatorFragmentInterface?.addContentFragment(fragment, true)
            } else {
                val editText =
                    binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                editText.requestFocus()
                dismissDialog()
            }
        } else {
            binding.searchTo.setQuery(location, false)
            binding.searchTo.clearFocus()

            if (binding.searchFrom.query.isNotEmpty()){
                val fragment = ConfirmReceiverAddressFragment()
                val bundle = Bundle()
                val geo = Geocoder(requireActivity(), Locale.getDefault())
                val addresses = geo.getFromLocation(getLatLngFromAddress(location)!!.latitude, getLatLngFromAddress(location)!!.longitude, 1);
                if (addresses.isNotEmpty()) {
                    bundle.putString("city", addresses[0].locality)
                    if (addresses[0].subLocality != null){
                        bundle.putString("area", addresses[0].subLocality)
                        bundle.putString("city", addresses[0].subLocality)
                    } else {
                        bundle.putString("area", addresses[0].locality)
                        bundle.putString("city", addresses[0].locality)
                    }
                }
                bundle.putString("sender_location_name", binding.searchFrom.query.toString())
                bundle.putString("receiver_location_name", binding.searchTo.query.toString())
                bundle.putDouble("latitude", getLatLngFromAddress(location)!!.latitude)
                bundle.putDouble("longitude", getLatLngFromAddress(location)!!.longitude)
                bundle.putString("receiver_name", requireArguments().getString("receiver_name"))
                bundle.putString("receiver_phone", requireArguments().getString("receiver_phone"))
                fragment.arguments = bundle
                dismissDialog()
                communicatorFragmentInterface?.addContentFragment(fragment, true)
            } else {
                val editText =
                    binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                editText.requestFocus()
                dismissDialog()
            }
        }
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