package com.airposted.bitoronbd.ui.location_set

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.FragmentLocationSetBinding
import com.airposted.bitoronbd.model.Prediction
import com.airposted.bitoronbd.model.SearchLocation
import com.airposted.bitoronbd.model.Term
import com.airposted.bitoronbd.ui.main.MainActivity
import com.airposted.bitoronbd.utils.ApiException
import com.airposted.bitoronbd.utils.NoInternetException
import com.airposted.bitoronbd.utils.hideKeyboard
import com.airposted.bitoronbd.utils.snackbar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.ArrayList

class LocationSetFragment : Fragment(), KodeinAware, CustomClickListener,
    OnMapReadyCallback, LocationListener {

    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var binding: FragmentLocationSetBinding
    private lateinit var viewModel: LocationSetViewModel
    private lateinit var list: SearchLocation
    private lateinit var mMap: GoogleMap
    private var latitude = ""
    private var longitude = ""
    private var locationName = ""
    private lateinit var locationManager: LocationManager
    private lateinit var builder: LocationSettingsRequest.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationSetBinding.inflate(inflater, container, false)


        viewModel = ViewModelProvider(this, factory).get(LocationSetViewModel::class.java)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapSearch1) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

        /*val bounce: Animation = AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.bounce
        )
        binding.marker.startAnimation(bounce)*/

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.myLocation.setOnClickListener {

            Permissions.check(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION,
                null,
                object : PermissionHandler() {
                    override fun onGranted() {

                        val manager:LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            getLocation()
                        } else {
                            val request = LocationRequest()
                                .setFastestInterval(0)
                                .setInterval(0)
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                            builder = LocationSettingsRequest.Builder()
                                .addLocationRequest(request)

                            val result = LocationServices.getSettingsClient(requireActivity())
                                .checkLocationSettings(
                                    builder.build()
                                )

                            result.addOnCompleteListener {
                                try {
                                    it.getResult(com.google.android.gms.common.api.ApiException::class.java)
                                } catch (e: com.google.android.gms.common.api.ApiException) {
                                    when (e.statusCode) {
                                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                            try {
                                                val resolvableApiException =
                                                    e as ResolvableApiException
                                                resolvableApiException.startResolutionForResult(
                                                    requireActivity(),
                                                    REQUEST_CHANGE_CODE
                                                )
                                            } catch (ex: ClassCastException) {

                                            }
                                        }
                                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                            val intent = Intent().apply {
                                                action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                            }
                                            startActivityForResult(
                                                intent,
                                                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                                            )
                                        }
                                    }
                                }
                            }
                            getLocation()
                        }
                    }

                    override fun onDenied(
                        context: Context?,
                        deniedPermissions: ArrayList<String>?
                    ) {
                        super.onDenied(context, deniedPermissions)
                    }

                    override fun onBlocked(
                        context: Context?,
                        blockedList: ArrayList<String>?
                    ): Boolean {
                        return super.onBlocked(context, blockedList)
                    }
                })
        }

        val editText =
            binding.search.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        editText.setHintTextColor(resources.getColor(R.color.gray))
        editText.setTextColor(resources.getColor(R.color.black))

        binding.setLocation.isEnabled = false

        binding.setLocation.setOnClickListener {

            val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
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
                PreferenceProvider(requireActivity()).saveSharedPreferences(
                    "currentLocation",
                    locationName
                )
                PreferenceProvider(requireActivity()).saveSharedPreferences("latitude", latitude)
                PreferenceProvider(requireActivity()).saveSharedPreferences("longitude", longitude)
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                binding.rootLayout.snackbar("Sorry!! We are currently not providing our service to this area")
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        //getLocation()
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
                            this@LocationSetFragment,
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

    override fun onItemClick(location: String) {
        hideKeyboard(requireActivity())
        binding.recyclerview.visibility = View.GONE
        val latLong = getLatLngFromAddress(location)
        val cameraPosition =
            CameraPosition.Builder().target(latLong)
                .zoom(16f).build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnCameraIdleListener {
            val center = mMap.cameraPosition.target
            val addresses = getAddressFromLatLng(LatLng(center.latitude, center.longitude))
            if (addresses == null) {
                //binding.editTextTextLocation.setText(getString(R.string.searching))
            } else {
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
                binding.search.setQuery(locationString, false)
                latitude = center.latitude.toString()
                longitude = center.longitude.toString()
                binding.setLocation.background = ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.after_button_bg
                )
                binding.setLocation.isEnabled = true
                locationName = locationString
                binding.recyclerview.visibility = View.GONE
                binding.loading.visibility = View.GONE
                binding.search.clearFocus()
            }
        }

        mMap.setOnCameraMoveStartedListener { reason ->
            when (reason) {
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                    //binding.editTextTextLocation.setText(getString(R.string.searching))
                    //productBinding.receiverAddress.visibility = View.GONE
                    binding.setLocation.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.before_button_bg
                    )
                    binding.setLocation.isEnabled = false
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
            val manager: LocationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )) {
                getLocation()
            }
        }
    }

    companion object {
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_CHANGE_CODE = 35
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            locationManager =
                requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                5f,
                this
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                5f,
                this
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        try {
            val cameraPosition =
                CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude))
                    .zoom(16f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
}