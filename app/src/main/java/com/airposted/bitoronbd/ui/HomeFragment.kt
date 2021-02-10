package com.airposted.bitoronbd.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.databinding.FragmentHomeBinding
import com.airposted.bitoronbd.ui.more.MoreViewModel
import com.airposted.bitoronbd.ui.more.MoreViewModelFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.*
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.ClassCastException
import java.util.*


class HomeFragment : Fragment(R.layout.fragment_home), KodeinAware, EasyPermissions.PermissionCallbacks, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var homeBinding: FragmentHomeBinding
    override val kodein by kodein()

    private val factory: MoreViewModelFactory by instance()
    private lateinit var viewModel: MoreViewModel

    var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var mFusedLocationClient: FusedLocationProviderClient? = null
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private val REQUEST_CHECK_CODE = 8989
    var result: PendingResult<LocationSettingsResult>? = null
    var mGoogleApiClient: GoogleApiClient? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        return homeBinding.root
    }

    private fun bindUI() {
        homeBinding.productBtn.setOnClickListener{
            Navigation.findNavController(requireView()).navigate(
                R.id.productFragment
            )
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mLocationRequest = LocationRequest()

        requestLocationPermission()

    }

    override fun onConnected(p0: Bundle?) {
        val mLocationRequest = LocationRequest.create()
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(
            mGoogleApiClient,
            builder.build()
        )

        result?.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {

                    requestLocationPermission()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->  // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try { // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                            requireActivity(),
                            REQUEST_CHECK_CODE
                        )
                    } catch (e: IntentSender.SendIntentException) { // Ignore the error.
                    }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {


                    requestLocationPermission()
                }
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPause() {
        super.onPause()
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                mLastLocation = location
                val geo = Geocoder(requireActivity(), Locale.getDefault())
                val addresses = geo.getFromLocation(location.latitude, location.longitude, 1);
                if (addresses.isEmpty()) {
                    homeBinding.address.text = getString(R.string.searching)
                }
                else {
                    homeBinding.address.text =
                        addresses[0].featureName + ", " + addresses[0].thoroughfare
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(requireActivity())
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    requestLocationPermission()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(
                        requireActivity(),
                        "Location not enabled, Please enable location to app work properly.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)) {
            AppSettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        bindUI()
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
            val manager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                mGoogleApiClient = GoogleApiClient.Builder(requireActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build()
                mGoogleApiClient?.connect()
            }
        } else {
            /*EasyPermissions.requestPermissions(
                this,
                "Please grant the location permission",
                MY_PERMISSIONS_REQUEST_LOCATION,
                perms
            )*/
            checkLocationPermission()
        }
    }
}