package com.airposted.bitoronbd.ui.location_set

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.EditText
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
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.ui.main.MainActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.maps.android.PolyUtil
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*


class LocationSetActivity : AppCompatActivity(), KodeinAware, CustomClickListener,
    OnMapReadyCallback, LocationListener {

    override val kodein by kodein()
    private val factory: LocationSetViewModelFactory by instance()
    private lateinit var binding: ActivityLocationSetBinding
    private lateinit var viewModel: LocationSetViewModel
    private lateinit var list: SearchLocation
    private lateinit var mMap: GoogleMap
    private lateinit var builder: LocationSettingsRequest.Builder
    private lateinit var locationManager: LocationManager
    private var latitude = ""
    private var longitude = ""
    private var locationName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_set)
        viewModel = ViewModelProvider(this, factory).get(LocationSetViewModel::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapSearch) as SupportMapFragment
        mapFragment.getMapAsync(this)


        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right)

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
            baseContext,
            R.anim.bounce
        )

        binding.marker.startAnimation(bounce)*/

        binding.setLocation.isEnabled = false

        binding.setLocation.setOnClickListener {

            val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
            val points: MutableList<LatLng> = ArrayList()
            points.add(LatLng(23.843974, 90.370339))
            points.add(LatLng(23.749419, 90.354546))
            points.add(LatLng(23.752248, 90.499085))
            points.add(LatLng(23.861558, 90.469903))

            //val polygon: Polygon = mMap.addPolygon(PolygonOptions().addAll(points))
            val contain = PolyUtil.containsLocation(latLng, points, true)

            if (contain) {
                PreferenceProvider(this).saveSharedPreferences(
                    "currentLocation",
                    locationName
                )
                PreferenceProvider(this).saveSharedPreferences("latitude", latitude)
                PreferenceProvider(this).saveSharedPreferences("longitude", longitude)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                binding.rootLayout.snackbar("Sorry!! We are currently not providing our service to this area")
            }
        }

        val editText =
            binding.search.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        editText.setHintTextColor(resources.getColor(R.color.gray))
        editText.setTextColor(resources.getColor(R.color.black))

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.myLocation.setOnClickListener {

            Permissions.check(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                null,
                object : PermissionHandler() {
                    override fun onGranted() {

                        val manager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            getLocation()
                        } else {
                            val request = LocationRequest()
                                .setFastestInterval(0)
                                .setInterval(0)
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                            builder = LocationSettingsRequest.Builder()
                                .addLocationRequest(request)

                            val result = LocationServices.getSettingsClient(this@LocationSetActivity)
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
                                                    this@LocationSetActivity,
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
            /*val cameraPosition =
                CameraPosition.Builder()
                    .target(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                    .zoom(18f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))*/
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
            val manager: LocationManager =
                this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )) {
                getLocation()
            }
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
                    this,
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
                        this,
                        R.drawable.before_button_bg
                    )
                    binding.setLocation.isEnabled = false
                }
            }
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

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            locationManager =
                this.getSystemService(LOCATION_SERVICE) as LocationManager
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

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    companion object {
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_CHANGE_CODE = 35
    }
}