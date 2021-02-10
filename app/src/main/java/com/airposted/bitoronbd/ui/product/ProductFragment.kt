package com.airposted.bitoronbd.ui.product

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentProductBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class ProductFragment : Fragment(), OnMapReadyCallback, KodeinAware {

    private lateinit var mMap: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLastLocation: Location
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var productBinding: FragmentProductBinding
    override val kodein by kodein()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        productBinding = FragmentProductBinding.inflate(inflater, container, false)

        return productBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mLocationRequest = LocationRequest()

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapProduct) as SupportMapFragment
        mapFragment.getMapAsync(this)
        requestLocationPermission()

        productBinding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        productBinding.myLocation.setOnClickListener{
            val cameraPosition =
                CameraPosition.Builder().target(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                    .zoom(16f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    override fun onPause() {
        super.onPause()

        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                mLastLocation = location

                val latLng = LatLng(location.latitude, location.longitude)

                val cameraPosition =
                    CameraPosition.Builder().target(LatLng(latLng.latitude, latLng.longitude))
                        .zoom(16f).build()
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                mMap.setOnCameraIdleListener {
                    val center = mMap.cameraPosition.target
                    val geo = Geocoder(requireActivity(), Locale.getDefault())
                    val addresses = geo.getFromLocation(center.latitude, center.longitude, 1)
                    if (addresses.isEmpty()) {
                        productBinding.address.text = getString(R.string.searching)
                    }
                    else {
                        var locationString:String
                        locationString = if (addresses[0].featureName == null){
                            ""
                        } else {
                            addresses[0].featureName
                        }
                        if (addresses[0].thoroughfare == null) {
                            locationString += ""
                        } else {
                            locationString = locationString + ", " + addresses[0].thoroughfare
                        }
                        productBinding.address.text = locationString
                    }
                    productBinding.receiverAddress.visibility = View.VISIBLE
                }

                mMap.setOnCameraMoveStartedListener { reason ->
                    when (reason) {
                        OnCameraMoveStartedListener.REASON_GESTURE -> {
                            productBinding.address.text = getString(R.string.searching)
                            productBinding.receiverAddress.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.isMyLocationEnabled = true

        mMap.uiSettings.isMyLocationButtonEnabled = false

        mLocationRequest.interval = 120000

        mLocationRequest.fastestInterval = 120000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest, mLocationCallback,
                    Looper.myLooper()
                )
            } else {
                requestLocationPermission()
            }
        } else {
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(1)
    fun requestLocationPermission() {
        val perms = Manifest.permission.ACCESS_FINE_LOCATION
        if (EasyPermissions.hasPermissions(requireActivity(), perms)) {
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } else {
            EasyPermissions.requestPermissions(
                requireActivity(),
                "Please grant the location permission",
                REQUEST_LOCATION_PERMISSION,
                perms
            )
        }
    }

}