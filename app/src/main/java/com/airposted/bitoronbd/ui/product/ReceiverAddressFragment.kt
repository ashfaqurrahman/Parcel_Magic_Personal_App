package com.airposted.bitoronbd.ui.product

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentReceiverAddressBinding
import com.airposted.bitoronbd.model.SearchLocation
import com.airposted.bitoronbd.ui.home.HomeViewModel
import com.airposted.bitoronbd.ui.home.HomeViewModelFactory
import com.airposted.bitoronbd.ui.location_set.*
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*


class ReceiverAddressFragment : Fragment(), KodeinAware, CustomClickListener {
    private lateinit var binding: FragmentReceiverAddressBinding
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private val factory1: HomeViewModelFactory by instance()
    private lateinit var viewModel: LocationSetViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var list: SearchLocation
    private var from = true
    private var focus = ""
    private var senderLocation = ""

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
        homeViewModel = ViewModelProvider(requireActivity(), factory1).get(HomeViewModel::class.java)
        bindUI()
    }

    private fun bindUI() {
        communicatorFragmentInterface = context as CommunicatorFragmentInterface

        binding.back.setOnClickListener {
            hideKeyboard(requireActivity())
            requireActivity().onBackPressed()
        }

        val editText =
            binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        editText.requestFocus()
        editText.showKeyboard()
        val editText1 =
            binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        //disableInput(editText1)

        homeViewModel.currentLocation.observe(viewLifecycleOwner, { locationDetailsWithName ->
            val locationDetailsImp = locationDetailsWithName

            binding.searchFrom.setQuery(locationDetailsImp.locationName, false)
            from = false
            binding.recyclerview.visibility = View.GONE

            val editText =
                binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            editText.requestFocus()
            editText.showKeyboard()
            //enableInput(editText)

            /*val cameraPosition =
                CameraPosition.Builder()
                    .target(
                        LatLng(
                            locationDetailsImp.latitude,
                            locationDetailsImp.longitude
                        )
                    )
                    .zoom(15.2f)                   // Sets the zoom
                    .build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))*/
        })

        //binding.searchFrom.findFocus()
        // for dialog
        /*if (PreferenceProvider(requireActivity()).getSharedPreferences("latitude") == null) {
            val editText =
                binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            editText.requestFocus()
            val editText1 =
                binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            disableInput(editText1)
        } else {
            val geo = Geocoder(requireActivity(), Locale.getDefault())
            val addresses = geo.getFromLocation(
                PreferenceProvider(requireActivity()).getSharedPreferences(
                    "latitude"
                )!!.toDouble(),
                PreferenceProvider(requireActivity()).getSharedPreferences("longitude")!!
                    .toDouble(),
                1
            )
            if (addresses.isNotEmpty()) {
                senderLocation = addresses[0].featureName + ", " + addresses[0].thoroughfare
                binding.searchFrom.setQuery(
                    addresses[0].featureName + ", " + addresses[0].thoroughfare,
                    false
                )
            }
            val editText =
                binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            editText.requestFocus()
        }*/

        binding.searchFrom.setOnQueryTextFocusChangeListener { view, isFocused ->
            if (isFocused) {
                focus = "from"
                from = true
            }
        }

        binding.searchTo.setOnQueryTextFocusChangeListener { view, isFocused ->
            if (isFocused) {
                focus = "to"
                from = false
            }
        }

        /*binding.map.setOnClickListener {
            communicatorFragmentInterface?.addContentFragment(LocationSetFragment(), true)
        }*/

        binding.searchFrom.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.searchFrom.isEnabled = false
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
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

    fun disableInput(editText: EditText) {
        editText.inputType = InputType.TYPE_NULL
        editText.setTextIsSelectable(false)
        editText.setOnKeyListener { v, keyCode, event ->
            true // Blocks input from hardware keyboards.
        }
    }

    fun enableInput(editText: EditText) {
        editText.inputType = InputType.TYPE_CLASS_TEXT
        editText.setTextIsSelectable(true)
        editText.setOnKeyListener { v, keyCode, event ->
            false // Blocks input from hardware keyboards.
        }
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
                    sb.append("&key=AIzaSyAJnceVASls_tIv4MiZFkzY1ZrVgu6GmW4")
                    sb.append("&components=country:bd")
                    list = viewModel.getLocations(sb.toString())
                    binding.loadingFrom.visibility = View.GONE
                    btnClose.visibility = View.VISIBLE
                    if (list.predictions.isNotEmpty()) {
                        val term = ArrayList<String>()
                        for (i in list.predictions.indices) {
                            if (list.predictions[i].terms.size > 1) {
                                var text = ""
                                for (j in 0 until list.predictions[i].terms.size - 1) {
                                    text += if (j > 0) {
                                        ", " + list.predictions[i].terms[j].value
                                    } else {
                                        list.predictions[i].terms[j].value
                                    }
                                }
                                term.add(text)
                            } else {
                                var text = ""
                                for (j in list.predictions[i].terms.indices) {
                                    text += if (j > 0) {
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
                    sb.append("&key=AIzaSyAJnceVASls_tIv4MiZFkzY1ZrVgu6GmW4")
                    sb.append("&components=country:bd")
                    list = viewModel.getLocations(sb.toString())
                    binding.loadingTo.visibility = View.GONE
                    btnClose.visibility = View.VISIBLE
                    if (list.predictions.isNotEmpty()) {
                        val term = ArrayList<String>()
                        for (i in list.predictions.indices) {
                            if (list.predictions[i].terms.size > 1) {
                                var text = ""
                                for (j in 0 until list.predictions[i].terms.size - 1) {
                                    text += if (j > 0) {
                                        ", " + list.predictions[i].terms[j].value
                                    } else {
                                        list.predictions[i].terms[j].value
                                    }
                                }
                                term.add(text)
                            } else {
                                var text = ""
                                for (j in list.predictions[i].terms.indices) {
                                    text += if (j > 0) {
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

        val latLng = LatLng(getLatLngFromAddress(location)!!.latitude, getLatLngFromAddress(
            location
        )!!.longitude)
        val points: MutableList<LatLng> = ArrayList()
        points.add(LatLng(23.888112, 90.383920))
        points.add(LatLng(23.834108, 90.344095))
        points.add(LatLng(23.753687, 90.341691))
        points.add(LatLng(23.703085, 90.375680))
        points.add(LatLng(23.685794, 90.463228))
        points.add(LatLng(23.717859, 90.534295))
        points.add(LatLng(23.795789, 90.562105))
        points.add(LatLng(23.845413, 90.519189))
        points.add(LatLng(23.881833, 90.452585))

        //val polygon: Polygon = mMap.addPolygon(PolygonOptions().addAll(points))
        val contain = PolyUtil.containsLocation(latLng, points, true)

        if (contain) {
            hideKeyboard(requireActivity())
            if (from) {
                binding.searchFrom.setQuery(location, false)
                binding.searchFrom.clearFocus()
                binding.recyclerview.visibility = View.GONE
                if (binding.searchTo.query.isNotEmpty()) {
                    if (getLatLngFromAddress(binding.searchFrom.query.toString()) != null){
                        if (getLatLngFromAddress(binding.searchTo.query.toString()) != null){
                            val fragment = ConfirmReceiverAddressFragment()
                            val bundle = Bundle()
                            /*val geo = Geocoder(requireActivity(), Locale.getDefault())
                            val addresses = geo.getFromLocation(
                                getLatLngFromAddress(location)!!.latitude, getLatLngFromAddress(
                                    location
                                )!!.longitude, 1
                            )
                            if (addresses.isNotEmpty()) {
                                bundle.putString("city", addresses[0].locality)
                                if (addresses[0].subLocality != null) {
                                    bundle.putString("area", addresses[0].subLocality)
                                    bundle.putString("city", addresses[0].subLocality)
                                } else {
                                    bundle.putString("area", addresses[0].locality)
                                    bundle.putString("city", addresses[0].locality)
                                }
                            }*/
                            bundle.putString("sender_location_name", binding.searchFrom.query.toString())
                            bundle.putString("receiver_location_name", binding.searchTo.query.toString())
                            bundle.putDouble("sender_latitude", getLatLngFromAddress(binding.searchFrom.query.toString())!!.latitude)
                            bundle.putDouble("sender_longitude", getLatLngFromAddress(binding.searchFrom.query.toString())!!.longitude)
                            bundle.putDouble("receiver_latitude", getLatLngFromAddress(binding.searchTo.query.toString())!!.latitude)
                            bundle.putDouble("receiver_longitude", getLatLngFromAddress(binding.searchTo.query.toString())!!.longitude)
                            bundle.putInt("parcel_quantity", requireArguments().getInt("parcel_quantity"))
                            bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
                            bundle.putInt("parcel_type", requireArguments().getInt("parcel_type"))
                            bundle.putString("receiver_name", requireArguments().getString("receiver_name"))
                            bundle.putString("receiver_phone", requireArguments().getString("receiver_phone"))
                            fragment.arguments = bundle
                            dismissDialog()
                            communicatorFragmentInterface?.addContentFragment(fragment, true)
                        } else {
                            val editText =
                                binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                            editText.requestFocus()
                            editText.showKeyboard()
                            dismissDialog()
                        }
                    } else {
                        val editText =
                            binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                        editText.requestFocus()
                        editText.showKeyboard()
                        dismissDialog()
                    }
                } else {
                    val editText =
                        binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                    editText.requestFocus()
                    editText.showKeyboard()
                    dismissDialog()
                }
            } else {
                binding.searchTo.setQuery(location, false)
                binding.searchTo.clearFocus()
                binding.recyclerview.visibility = View.GONE

                if (binding.searchFrom.query.isNotEmpty()) {

                    if (getLatLngFromAddress(binding.searchFrom.query.toString()) != null){
                        if (getLatLngFromAddress(binding.searchTo.query.toString()) != null){
                            val fragment = ConfirmReceiverAddressFragment()
                            val bundle = Bundle()
                            /*val geo = Geocoder(requireActivity(), Locale.getDefault())
                            val addresses = geo.getFromLocation(
                                getLatLngFromAddress(location)!!.latitude, getLatLngFromAddress(
                                    location
                                )!!.longitude, 1
                            );
                            if (addresses.isNotEmpty()) {
                                bundle.putString("city", addresses[0].locality)
                                if (addresses[0].subLocality != null) {
                                    bundle.putString("area", addresses[0].subLocality)
                                    bundle.putString("city", addresses[0].subLocality)
                                } else {
                                    bundle.putString("area", addresses[0].locality)
                                    bundle.putString("city", addresses[0].locality)
                                }
                            }*/
                            bundle.putString("sender_location_name", binding.searchFrom.query.toString())
                            bundle.putString("receiver_location_name", binding.searchTo.query.toString())
                            bundle.putDouble("sender_latitude", getLatLngFromAddress(binding.searchFrom.query.toString())!!.latitude)
                            bundle.putDouble("sender_longitude", getLatLngFromAddress(binding.searchFrom.query.toString())!!.longitude)
                            bundle.putDouble("receiver_latitude", getLatLngFromAddress(binding.searchTo.query.toString())!!.latitude)
                            bundle.putDouble("receiver_longitude", getLatLngFromAddress(binding.searchTo.query.toString())!!.longitude)
                            bundle.putInt("parcel_quantity", requireArguments().getInt("parcel_quantity"))
                            bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
                            bundle.putInt("parcel_type", requireArguments().getInt("parcel_type"))
                            bundle.putString("receiver_name", requireArguments().getString("receiver_name"))
                            bundle.putString("receiver_phone", requireArguments().getString("receiver_phone"))
                            fragment.arguments = bundle
                            dismissDialog()
                            communicatorFragmentInterface?.addContentFragment(fragment, true)
                        } else {
                            val editText =
                                binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                            editText.requestFocus()
                            editText.showKeyboard()
                            dismissDialog()
                        }
                    } else {
                        val editText =
                            binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                        editText.requestFocus()
                        editText.showKeyboard()
                        dismissDialog()
                    }
                } else {
                    val editText =
                        binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                    editText.requestFocus()
                    editText.showKeyboard()
                    dismissDialog()
                }
            }
        } else {
            dismissDialog()
            binding.rootLayout.snackbar("Sorry!! We are currently not providing our service to this area")
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