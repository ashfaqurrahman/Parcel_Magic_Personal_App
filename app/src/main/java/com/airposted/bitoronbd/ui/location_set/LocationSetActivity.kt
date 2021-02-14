package com.airposted.bitoronbd.ui.location_set

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.ActivityLocationSetBinding
import com.airposted.bitoronbd.model.Prediction
import com.airposted.bitoronbd.model.SearchLocation
import com.airposted.bitoronbd.utils.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*


class LocationSetActivity : AppCompatActivity(), KodeinAware, CustomClickListener,
    OnMapReadyCallback {

    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var binding: ActivityLocationSetBinding
    private lateinit var viewModel: LocationSetViewModel
    private lateinit var list: SearchLocation
    private lateinit var mMap: GoogleMap
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLastLocation: Location
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_set)
        viewModel = ViewModelProvider(this, factory).get(LocationSetViewModel::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapSearch) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = LocationRequest()

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

        val bounce: Animation = AnimationUtils.loadAnimation(
            baseContext,
            R.anim.bounce
        ) // load the animation from the resource


        binding.marker.startAnimation(bounce)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.myLocation.setOnClickListener {
            val cameraPosition =
                CameraPosition.Builder()
                    .target(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                    .zoom(18f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

    }

    private fun location(location: String) {
        if (location.length > 2) {
            val btnClose: ImageView = binding.search.findViewById(R.id.search_close_btn)
            btnClose.visibility = View.GONE
            binding.loading.visibility = View.VISIBLE
            binding.recyclerview.visibility = View.VISIBLE
            //binding.progressBar.show()
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
                            this@LocationSetActivity,
                        )
                        binding.recyclerview.layoutManager = GridLayoutManager(
                            this@LocationSetActivity,
                            1
                        )
                        //binding.recyclerview.addItemDecoration(GridSpacingItemDecoration(3, dpToPx(4), true))
                        binding.recyclerview.itemAnimator = DefaultItemAnimator()
                        binding.recyclerview.adapter = myRecyclerViewAdapter
                    } else {
                        binding.recyclerview.visibility = View.GONE
                    }

                    //binding.progressBar.hide()
                } catch (e: ApiException) {
                    //binding.progressBar.hide()
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    //binding.progressBar.hide()
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                }
            }
        } else {
            //binding.progressBar.hide()
            binding.recyclerview.visibility = View.GONE
        }
    }

    private fun getLatLngFromAddress(address: String): LatLng? {
        val geocoder = Geocoder(this)
        val addressList: List<Address>?
        return try {
            addressList = geocoder.getFromLocationName(address, 1)
            if (addressList != null) {
                val singleaddress = addressList[0]
                LatLng(singleaddress.latitude, singleaddress.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng): Address? {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        return try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
            addresses?.get(0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onItemClick(location: Prediction) {
        hideKeyboard(this)
//        binding.editTextTextLocation.setText(location.description)
        binding.recyclerview.visibility = View.GONE

        val latLong = getLatLngFromAddress(location.description)

        val cameraPosition =
            CameraPosition.Builder().target(latLong)
                .zoom(16f).build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                mLastLocation = location


                }
            }
        }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mMap.isMyLocationEnabled = true

        mMap.uiSettings.isMyLocationButtonEnabled = false

        mLocationRequest.interval = 120000

        mLocationRequest.fastestInterval = 120000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest, mLocationCallback,
                    Looper.myLooper()
                )
            }
        } else {
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
            )
        }

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
                }
            }
        }
    }
}