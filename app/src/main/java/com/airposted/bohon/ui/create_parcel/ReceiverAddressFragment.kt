package com.airposted.bohon.ui.create_parcel

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bohon.R
import com.airposted.bohon.data.db.Location
import com.airposted.bohon.databinding.FragmentReceiverAddressBinding
import com.airposted.bohon.model.SearchLocation
import com.airposted.bohon.ui.adapter.LocationSetRecyclerViewAdapter
import com.airposted.bohon.ui.home.HomeViewModel
import com.airposted.bohon.ui.home.HomeViewModelFactory
import com.airposted.bohon.ui.location_set.*
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface
import com.airposted.bohon.utils.*
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
import java.lang.IndexOutOfBoundsException

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
    private var fromLatitude = 0.0
    private var fromLongitude = 0.0
    private var fromLocationName = ""
    private var toLatitude = 0.0
    private var toLongitude = 0.0
    private var toLocationName = ""
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var locations: List<Location>

    private var firstStart = true

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

        val fromEditText =
            binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        fromEditText.requestFocus()
        fromEditText.showKeyboard()

        homeViewModel.currentLocation.observe(viewLifecycleOwner, { locationDetailsWithName ->
            fromLocationName = locationDetailsWithName.locationName
            fromLatitude = locationDetailsWithName.latitude
            fromLongitude = locationDetailsWithName.longitude

            binding.searchFrom.setQuery(locationDetailsWithName.locationName, false)
            from = false
            binding.recyclerview.visibility = View.GONE

            val toEditText = binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            toEditText.requestFocus()
            toEditText.showKeyboard()
        })

        homeViewModel.locations.observe(viewLifecycleOwner, {
            locations = it
            locationFrom("")
            initRecyclerView(locations)
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

        binding.searchFrom.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.searchTo.setQuery(toLocationName, false)
                binding.bottomSheet.visibility = View.VISIBLE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                from = true
            }
        }

        binding.searchTo.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.searchFrom.setQuery(fromLocationName, false)
                binding.bottomSheet.visibility = View.VISIBLE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                from = false
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = 0
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
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.searchFrom.setQuery(fromLocationName, false)
                    binding.searchTo.setQuery(toLocationName, false)
                }
                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    binding.searchFrom.setQuery(fromLocationName, false)
                    binding.searchTo.setQuery(toLocationName, false)
                }
            }
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
                locationFrom(newText)
                return false
            }
        })

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapSearch) as SupportMapFragment
        mapFragment.getMapAsync(this)


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
                        bundle.putDouble("sender_latitude", fromLatitude)
                        bundle.putDouble("sender_longitude", fromLongitude)
                        bundle.putDouble("receiver_latitude", toLatitude)
                        bundle.putDouble("receiver_longitude", toLongitude)
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
                        bundle.putDouble("sender_latitude", fromLatitude)
                        bundle.putDouble("sender_longitude", fromLongitude)
                        bundle.putDouble("receiver_latitude", toLatitude)
                        bundle.putDouble("receiver_longitude", toLongitude)
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
    }

    private fun initRecyclerView(list: List<Location>) {
        binding.recyclerview.visibility = View.VISIBLE
        val myRecyclerViewAdapter = LocationSetRecyclerViewAdapter(
            list,
            this@ReceiverAddressFragment,
        )
        binding.recyclerview.layoutManager = GridLayoutManager(
            requireActivity(),
            1
        )
        binding.recyclerview.itemAnimator = DefaultItemAnimator()
        binding.recyclerview.adapter = myRecyclerViewAdapter
    }

    private fun locationFrom(location: String) {
        if (location.isNotEmpty()) {
            binding.recyclerview.visibility = View.GONE
            //setProgressDialog(requireActivity())
            //val btnClose: ImageView = binding.searchFrom.findViewById(R.id.search_close_btn)
            //btnClose.visibility = View.GONE
            binding.progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val locationLat = 23.777176
                    val LocationLng = 90.399452
                    val sb =
                        StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?")
                    sb.append("input=$location")
                    sb.append("&key=" + getString(R.string.google_maps_key))
                    sb.append("&location=$locationLat,$LocationLng")
                    sb.append("&radius=20000&strictbounds")
                    list = viewModel.getLocations(sb.toString())
                    if (list.predictions.isNotEmpty()) {
                        val term = ArrayList<Location>()
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

                                /*val sb =
                                    StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?")
                                sb.append("input=${list.predictions[i].description}")
                                sb.append("&placeid=${list.predictions[i].placeId}")
                                sb.append("&key=" + getString(R.string.google_maps_key))
                                sb.append("&components=country:bd")
                                Log.e("aaaa", sb.toString())
                                val list = viewModel.getLocationDetails(sb.toString())*/

                                term.add(Location(text))
                            } else {
                                var text = ""
                                for (j in list.predictions[i].terms.indices) {
                                    text += if (j > 0) {
                                        ", " + list.predictions[i].terms[j].value
                                    } else {
                                        list.predictions[i].terms[j].value
                                    }
                                }
                                /*val sb =
                                    StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?")
                                sb.append("input=${list.predictions[i].description}")
                                sb.append("&placeid=${list.predictions[i].placeId}")
                                sb.append("&key=" + getString(R.string.google_maps_key))
                                sb.append("&components=country:bd")
                                Log.e("aaaa", sb.toString())
                                val list = viewModel.getLocationDetails(sb.toString())*/

                                term.add(Location(text))
                            }
                        }
                        //dismissDialog()
                        initRecyclerView(term)
                        binding.progress.visibility = View.GONE
                        //btnClose.visibility = View.VISIBLE
                    } else {
                        binding.recyclerview.visibility = View.GONE
                    }
                } catch (e: IndexOutOfBoundsException) {
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: com.google.android.gms.common.api.ApiException) {
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: ApiException) {
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                }
            }
        } else {
            initRecyclerView(locations)
        }
    }

    override fun onItemClick(location: Location) {
        //setProgressDialog(requireActivity())

        val latLng: LatLng?

        val geocoder = Geocoder(requireActivity())
        val addressList: List<Address>?
        try {

            binding.progress.visibility = View.VISIBLE
            addressList = geocoder.getFromLocationName(location.name, 1)
            if (addressList != null) {
                val singleAddress = addressList[0]
                latLng = LatLng(singleAddress.latitude, singleAddress.longitude)

                val points: MutableList<LatLng> = ArrayList()
                points.add(LatLng(23.892451, 90.359477))
                points.add(LatLng(23.889343, 90.357292))
                points.add(LatLng(23.882349, 90.358020))
                points.add(LatLng(23.877132, 90.349522))
                points.add(LatLng(23.864921, 90.349522))
                points.add(LatLng(23.851822, 90.338963))
                points.add(LatLng(23.846715, 90.336779))
                points.add(LatLng(23.841276, 90.341270))
                points.add(LatLng(23.834399, 90.338275))
                points.add(LatLng(23.832251, 90.336846))
                points.add(LatLng(23.830197, 90.337255))
                points.add(LatLng(23.826856, 90.342176))
                points.add(LatLng(23.820024, 90.336254))
                points.add(LatLng(23.818333, 90.337341))
                points.add(LatLng(23.817862, 90.340560))
                points.add(LatLng(23.811541, 90.338329))
                points.add(LatLng(23.802550, 90.342321))
                points.add(LatLng(23.797681, 90.339144))
                points.add(LatLng(23.786293, 90.337687))
                points.add(LatLng(23.783662, 90.334982))
                points.add(LatLng(23.779146, 90.337428))
                points.add(LatLng(23.774590, 90.333738))
                points.add(LatLng(23.770625, 90.343651))
                points.add(LatLng(23.764732, 90.338458))
                points.add(LatLng(23.752791, 90.329919))
                points.add(LatLng(23.749649, 90.329190))
                points.add(LatLng(23.748864, 90.338845))
                points.add(LatLng(23.734879, 90.354337))
                points.add(LatLng(23.715549, 90.360088))
                points.add(LatLng(23.708594, 90.364035))
                points.add(LatLng(23.708711, 90.388541))
                points.add(LatLng(23.710242, 90.399056))
                points.add(LatLng(23.690558, 90.423988))
                points.add(LatLng(23.698671, 90.477718))
                points.add(LatLng(23.719103, 90.500043))
                points.add(LatLng(23.732307, 90.496787))
                points.add(LatLng(23.734195, 90.492152))
                points.add(LatLng(23.744516, 90.490791))
                points.add(LatLng(23.750409, 90.485384))
                points.add(LatLng(23.754573, 90.488560))
                points.add(LatLng(23.759287, 90.482895))
                points.add(LatLng(23.768322, 90.482381))
                points.add(LatLng(23.771386, 90.482982))
                points.add(LatLng(23.778222, 90.477230))
                points.add(LatLng(23.779081, 90.473453))
                points.add(LatLng(23.783951, 90.470578))
                points.add(LatLng(23.792276, 90.480664))
                points.add(LatLng(23.806844, 90.484912))
                points.add(LatLng(23.821328, 90.486415))
                points.add(LatLng(23.832165, 90.486674))
                points.add(LatLng(23.831145, 90.482382))
                points.add(LatLng(23.842768, 90.472424))
                points.add(LatLng(23.857364, 90.474568))
                points.add(LatLng(23.862387, 90.474740))
                points.add(LatLng(23.873221, 90.465298))
                points.add(LatLng(23.883422, 90.461007))
                points.add(LatLng(23.894175, 90.457917))
                points.add(LatLng(23.900295, 90.439546))
                points.add(LatLng(23.890403, 90.423495))
                points.add(LatLng(23.890643, 90.423929))
                points.add(LatLng(23.883815, 90.413027))
                points.add(LatLng(23.882245, 90.402813))
                points.add(LatLng(23.880911, 90.393630))
                points.add(LatLng(23.889152, 90.392257))
                points.add(LatLng(23.898336, 90.385476))
                points.add(LatLng(23.899356, 90.379294))
                points.add(LatLng(23.896766, 90.369593))

                //val polygon: Polygon = mMap.addPolygon(PolygonOptions().addAll(points))
                val contain = PolyUtil.containsLocation(latLng, points, true)

                if (contain) {
                    hideKeyboard(requireActivity())

                    if (from) {

                        fromLocationName = location.name!!
                        fromLatitude = latLng.latitude
                        fromLongitude = latLng.longitude

                        binding.searchFrom.setQuery(location.name!!, false)
                        binding.searchFrom.clearFocus()
                        binding.recyclerview.visibility = View.GONE
                        if (binding.searchTo.query.isNotEmpty()) {
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
                            var addressAvailable = false
                            for (i in locations.indices) {
                                addressAvailable = location == locations[i]
                            }

                            if (!addressAvailable) {
                                homeViewModel.saveAddress(Location(location.name))
                            }

                            bundle.putString("sender_location_name", binding.searchFrom.query.toString())
                            bundle.putString("receiver_location_name", binding.searchTo.query.toString())
                            bundle.putDouble("sender_latitude", fromLatitude)
                            bundle.putDouble("sender_longitude", fromLongitude)
                            bundle.putDouble("receiver_latitude", toLatitude)
                            bundle.putDouble("receiver_longitude", toLongitude)
                            bundle.putInt("parcel_quantity", requireArguments().getInt("parcel_quantity"))
                            bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
                            bundle.putInt("parcel_type", requireArguments().getInt("parcel_type"))
                            bundle.putString("receiver_name", requireArguments().getString("receiver_name"))
                            bundle.putString("receiver_phone", requireArguments().getString("receiver_phone"))
                            fragment.arguments = bundle
                            binding.progress.visibility = View.GONE
                            communicatorFragmentInterface?.addContentFragment(fragment, true)
                        } else {
                            val editText =
                                binding.searchTo.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                            editText.requestFocus()
                            editText.showKeyboard()
                            binding.progress.visibility = View.GONE
                        }
                    } else {

                        toLocationName = location.name!!
                        toLatitude = latLng.latitude
                        toLongitude = latLng.longitude

                        binding.searchTo.setQuery(location.name!!, false)
                        binding.searchTo.clearFocus()
                        binding.recyclerview.visibility = View.GONE

                        if (binding.searchFrom.query.isNotEmpty()) {

                            var addressAvailable = false
                            for (i in locations.indices) {
                                addressAvailable = location == locations[i]
                            }

                            if (!addressAvailable) {
                                homeViewModel.saveAddress(Location(location.name))
                            }

                            val fragment = ConfirmReceiverAddressFragment()
                            val bundle = Bundle()
                            bundle.putString("sender_location_name", binding.searchFrom.query.toString())
                            bundle.putString("receiver_location_name", binding.searchTo.query.toString())
                            bundle.putDouble("sender_latitude", fromLatitude)
                            bundle.putDouble("sender_longitude", fromLongitude)
                            bundle.putDouble("receiver_latitude", toLatitude)
                            bundle.putDouble("receiver_longitude", toLongitude)
                            bundle.putInt("parcel_quantity", requireArguments().getInt("parcel_quantity"))
                            bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
                            bundle.putInt("parcel_type", requireArguments().getInt("parcel_type"))
                            bundle.putString("receiver_name", requireArguments().getString("receiver_name"))
                            bundle.putString("receiver_phone", requireArguments().getString("receiver_phone"))
                            fragment.arguments = bundle
                            binding.progress.visibility = View.GONE
                            communicatorFragmentInterface?.addContentFragment(fragment, true)
                        } else {
                            val editText =
                                binding.searchFrom.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                            editText.requestFocus()
                            editText.showKeyboard()
                            binding.progress.visibility = View.GONE
                        }
                    }
                } else {
                    binding.progress.visibility = View.GONE
                    binding.rootLayout.snackbar("Sorry!! We are currently not providing our service to this area")
                }
            } else {

            }
        } catch (e: Exception) {
            e.printStackTrace()

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

                    val geocoder = Geocoder(requireActivity())
                    val addressList: List<Address>?
                    try {
                        addressList = geocoder.getFromLocationName(it[0].name, 1)
                        if (addressList != null) {
                            val singleAddress = addressList[0]
                            val latLng = LatLng(singleAddress.latitude, singleAddress.longitude)

                            val cameraPosition =
                                CameraPosition.Builder()
                                    .target(
                                        LatLng(
                                            latLng.latitude,
                                            latLng.longitude!!
                                        )
                                    )
                                    .zoom(15.2f)                   // Sets the zoom
                                    .build()
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        } else {
                            val cameraPosition =
                                CameraPosition.Builder()
                                    .target(
                                        LatLng(
                                            23.777176,
                                            90.399452
                                        )
                                    )
                                    .zoom(11f)                   // Sets the zoom
                                    .build()
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {

                }
            })
        }

        mMap.setOnCameraIdleListener {
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
                    if (addresses.subLocality == null) {
                        locationString += ""
                    } else {
                        locationString = locationString + ", " + addresses.subLocality
                    }
                    if (addresses.locality == null) {
                        locationString += ""
                    } else {
                        locationString = locationString + ", " + addresses.locality
                    }

                    /*var addressAvailable = false
                    for (i in locations.indices) {
                        addressAvailable = locationString == locations[i]
                    }

                    if (!addressAvailable) {
                        homeViewModel.saveAddress(Location(locationString, center.latitude, center.longitude))
                    }*/

                    if (!firstStart) {
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
                    }

                } else {
                    dismissDialog()
                    binding.rootLayout.snackbar("Sorry!! We are currently not providing our service to this area")
                }

            }
        }

        mMap.setOnCameraMoveListener {

        }

        mMap.setOnCameraMoveStartedListener { reason ->
            when (reason) {
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                    firstStart = false
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