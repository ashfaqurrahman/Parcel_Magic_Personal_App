package com.airposted.bitoronbd.ui.location_set

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
    private lateinit var list:SearchLocation
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_set)
        viewModel = ViewModelProvider(this, factory).get(LocationSetViewModel::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapSearch) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.editTextTextLocation.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                location(s.toString())
            }
        })
    }

    private fun location(location: String) {
        if (location.length > 2) {
            binding.recyclerview.visibility = View.VISIBLE
            binding.progressBar.show()
            lifecycleScope.launch {
                try {
                    val sb = StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json?")
                    sb.append("input=$location")
                    sb.append("&key=AIzaSyB8gzYgvsy-1TufBYLYaD58EYDTWUZBWZQ")
                    sb.append("&components=country:bd")
                    list = viewModel.getLocations(sb.toString())

                    if (list.predictions.isNotEmpty()){
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

                    binding.progressBar.hide()
                } catch (e: ApiException) {
                    binding.progressBar.hide()
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    binding.progressBar.hide()
                    binding.rootLayout.snackbar(e.message!!)
                    e.printStackTrace()
                }
            }
        } else {
            binding.progressBar.hide()
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

    override fun onItemClick(location: Prediction) {
        hideKeyboard(this)
        binding.editTextTextLocation.setText(location.description)
        binding.recyclerview.visibility = View.GONE

        val latLong = getLatLngFromAddress(location.description)

        val cameraPosition =
            CameraPosition.Builder().target(latLong)
                .zoom(16f).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
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

        mMap.setOnCameraIdleListener {
            val center = mMap.cameraPosition.target
            val geo = Geocoder(this, Locale.getDefault())
            val addresses = geo.getFromLocation(center.latitude, center.longitude, 1)
            if (addresses.isEmpty()) {
                //binding.editTextTextLocation.setText(getString(R.string.searching))
            } else {
                var locationString: String
                locationString = if (addresses[0].featureName == null) {
                    ""
                } else {
                    addresses[0].featureName
                }
                if (addresses[0].thoroughfare == null) {
                    locationString += ""
                } else {
                    locationString = locationString + ", " + addresses[0].thoroughfare
                }
                binding.editTextTextLocation.setText(locationString)
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