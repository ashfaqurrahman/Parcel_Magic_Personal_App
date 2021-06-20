package com.airposted.bitoronbd.ui.product

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.db.Location
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentReceiverAddressBinding
import com.airposted.bitoronbd.model.LocationDetailsWithName
import com.airposted.bitoronbd.model.SearchLocation
import com.airposted.bitoronbd.ui.home.HomeViewModel
import com.airposted.bitoronbd.ui.home.HomeViewModelFactory
import com.airposted.bitoronbd.ui.location_set.*
import com.airposted.bitoronbd.ui.main.CommunicatorFragmentInterface
import com.airposted.bitoronbd.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class ReceiverAddressFragment : Fragment(), KodeinAware, CustomClickListener, OnMapReadyCallback {
    private lateinit var binding: FragmentReceiverAddressBinding
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private val factory1: HomeViewModelFactory by instance()
    private lateinit var viewModel: LocationSetViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var list: SearchLocation
    private lateinit var mMap: GoogleMap
    private var from = true
    private var focus = ""
    private var fromLatitude = 0.0
    private var fromLongitude = 0.0
    private var fromLocationName = ""
    private var toLatitude = 0.0
    private var toLongitude = 0.0
    private var toLocationName = ""
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var locations: List<String>

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

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapSearch) as SupportMapFragment
        mapFragment.getMapAsync(this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = 200

        homeViewModel.runs.observe(viewLifecycleOwner, {
            locations = it
            val myRecyclerViewAdapter = LocationSetRecyclerViewAdapter(
                it,
                it,
                this@ReceiverAddressFragment,
            )
            binding.recyclerview.layoutManager = GridLayoutManager(
                requireActivity(),
                1
            )
            binding.recyclerview.itemAnimator = DefaultItemAnimator()
            binding.recyclerview.adapter = myRecyclerViewAdapter
        })

        binding.back.setOnClickListener {
            hideKeyboard(requireActivity())
            requireActivity().onBackPressed()
        }

        binding.map.setOnClickListener {
            binding.searchFrom.setQuery(fromLocationName, false)
            binding.searchTo.setQuery(toLocationName, false)
            binding.bottomSheet.visibility = View.GONE
            binding.searchFrom.clearFocus()
            binding.searchTo.clearFocus()

        }

        binding.searchFrom.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.searchTo.setQuery(toLocationName, false)
                //binding.recyclerview.visibility = View.GONE
                binding.bottomSheet.visibility = View.VISIBLE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                focus = "from"
                from = true
            }
        }

        binding.searchTo.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.searchFrom.setQuery(fromLocationName, false)
                //binding.recyclerview.visibility = View.GONE
                binding.bottomSheet.visibility = View.VISIBLE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                focus = "to"
                from = false
            }
        }

        binding.setLocation.setOnClickListener {
            if (from){
                if (binding.searchFrom.query.isNullOrEmpty()) {
                    binding.searchFrom.requestFocus()
                } else {
                    if (binding.searchTo.query.isNullOrEmpty()) {
                        binding.searchTo.requestFocus()
                    } else {
                        val fragment = ConfirmReceiverAddressFragment()
                        val bundle = Bundle()

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
                        communicatorFragmentInterface?.addContentFragment(fragment, true)
                    }
                }
            } else {
                if (binding.searchTo.query.isNullOrEmpty()) {
                    binding.searchTo.requestFocus()
                } else {
                    if (binding.searchFrom.query.isNullOrEmpty()) {
                        binding.searchFrom.requestFocus()
                    } else {
                        val fragment = ConfirmReceiverAddressFragment()
                        val bundle = Bundle()

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
                        communicatorFragmentInterface?.addContentFragment(fragment, true)
                    }
                }
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {

                    binding.searchFrom.clearFocus()
                    binding.searchTo.clearFocus()
                }

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (from) {
                        binding.searchFrom.requestFocus()
                    } else {
                        binding.searchTo.requestFocus()
                    }
                }

            }
        })

        val editText =
            binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        editText.requestFocus()
        editText.showKeyboard()

        homeViewModel.currentLocation.observe(viewLifecycleOwner, { locationDetailsWithName ->
            val locationDetailsImp = locationDetailsWithName
            fromLocationName = locationDetailsImp.locationName
            fromLatitude = locationDetailsImp.latitude
            fromLongitude = locationDetailsImp.longitude

            binding.searchFrom.setQuery(locationDetailsImp.locationName, false)
            from = false
            binding.recyclerview.visibility = View.GONE

            val editText = binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            editText.requestFocus()
            editText.showKeyboard()
        })

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

    private fun locationFrom(location: String) {
        if (location.isNotEmpty()) {
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
                        val description = ArrayList<String>()
                        for (i in list.predictions.indices) {
                            description.add(list.predictions[i].description)
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
                            description,
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
            //binding.recyclerview.visibility = View.GONE
            val myRecyclerViewAdapter = LocationSetRecyclerViewAdapter(
                locations,
                locations,
                this@ReceiverAddressFragment,
            )
            binding.recyclerview.layoutManager = GridLayoutManager(
                requireActivity(),
                1
            )
            binding.recyclerview.itemAnimator = DefaultItemAnimator()
            binding.recyclerview.adapter = myRecyclerViewAdapter
        }
    }

    private fun locationTo(location: String) {
        if (location.isNotEmpty()) {
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
                        val description = ArrayList<String>()
                        for (i in list.predictions.indices) {

                            description.add(list.predictions[i].description)

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
                            description,
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
            //binding.recyclerview.visibility = View.GONE
            val myRecyclerViewAdapter = LocationSetRecyclerViewAdapter(
                locations,
                locations,
                this@ReceiverAddressFragment,
            )
            binding.recyclerview.layoutManager = GridLayoutManager(
                requireActivity(),
                1
            )
            binding.recyclerview.itemAnimator = DefaultItemAnimator()
            binding.recyclerview.adapter = myRecyclerViewAdapter
        }
    }

    override fun onItemClick(location: String, description: String) {
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
            var addressAvailable = false
            for (i in locations.indices) {
                addressAvailable = location == locations[i]
            }

            if (!addressAvailable) {
                homeViewModel.saveAddress(Location(location, getLatLngFromAddress(location)?.latitude, getLatLngFromAddress(location)?.longitude))
            }

            if (from) {

                fromLocationName = location
                fromLatitude = getLatLngFromAddress(location)?.latitude!!
                fromLongitude = getLatLngFromAddress(location)?.longitude!!

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

                toLocationName = location
                toLatitude = getLatLngFromAddress(location)?.latitude!!
                toLongitude = getLatLngFromAddress(location)?.longitude!!

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (fromLocationName != ""){
            //binding.searchTo.requestFocus()
            val cameraPosition =
                CameraPosition.Builder()
                    .target(
                        LatLng(
                            fromLatitude,
                            fromLongitude
                        )
                    )
                    .zoom(15.2f)                   // Sets the zoom
                    .build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } else {
            binding.searchFrom.requestFocus()

            homeViewModel.getLastLocation.observe(viewLifecycleOwner, {
                if(it.isNotEmpty()){
                    val cameraPosition =
                        CameraPosition.Builder()
                            .target(
                                LatLng(
                                    it[0].latitude!!,
                                    it[0].longitude!!
                                )
                            )
                            .zoom(15.2f)                   // Sets the zoom
                            .build()
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            })
        }

        mMap.setOnCameraIdleListener {

        }

        mMap.setOnCameraMoveListener {
            val center = mMap.cameraPosition.target
            val addresses = getAddressFromLatLng(LatLng(center.latitude, center.longitude))
            if (addresses == null) {
                //binding.editTextTextLocation.setText(getString(R.string.searching))
            } else {

                val latLng = LatLng(center.latitude, center.longitude)
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
                    var locationString: String
                    locationString = if (addresses.featureName == null) {
                        ""
                    } else {
                        addresses.featureName
                    }
                    if (addresses.thoroughfare == null) {
                        locationString += ""
                    } else {
                        locationString = locationString + ", " + addresses.thoroughfare
                    }

                    var addressAvailable = false
                    for (i in locations.indices) {
                        addressAvailable = addresses.getAddressLine(0) == locations[i]
                    }

                    if (!addressAvailable) {
                        homeViewModel.saveAddress(Location(addresses.getAddressLine(0), center.latitude, center.longitude))
                    }

                    if (from){
                        binding.searchFrom.setQuery(locationString, false)
                        binding.recyclerview.visibility = View.GONE
                        //binding.address.text = locationString
                        fromLatitude = center.latitude
                        fromLongitude = center.longitude
                        fromLocationName = locationString
                        binding.searchFrom.clearFocus()
                        //binding.searchTo.requestFocus()
                    } else {
                        binding.searchTo.setQuery(locationString, false)
                        binding.recyclerview.visibility = View.GONE
                        //binding.address.text = locationString
                        toLatitude = center.latitude
                        toLongitude = center.longitude
                        toLocationName = locationString
                        binding.searchTo.clearFocus()
                    }
                } else {
                    dismissDialog()
                    binding.rootLayout.snackbar("Sorry!! We are currently not providing our service to this area")
                }

            }
        }

        mMap.setOnCameraMoveStartedListener { reason ->
            when (reason) {
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                    //binding.editTextTextLocation.setText(getString(R.string.searching))
                    //productBinding.receiverAddress.visibility = View.GONE
                    /*binding.setLocation.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.before_button_bg
                    )*/
                    //binding.address.text = "Loading..."
                }
            }
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng): Address? {
        val geoCoder = Geocoder(requireActivity())
        val addresses: List<Address>?
        return try {
            addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
            addresses?.get(0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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