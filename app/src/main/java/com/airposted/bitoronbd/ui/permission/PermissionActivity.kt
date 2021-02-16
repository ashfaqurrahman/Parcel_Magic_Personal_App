package com.airposted.bitoronbd.ui.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.databinding.ActivityPermissionBinding
import com.airposted.bitoronbd.ui.location_set.LocationSetActivity
import com.airposted.bitoronbd.ui.main.MainActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.lang.ClassCastException
import java.util.*


class PermissionActivity : AppCompatActivity(), LocationListener {

    private lateinit var permissionBinding: ActivityPermissionBinding

    private lateinit var locationManager: LocationManager
    private lateinit var builder: LocationSettingsRequest.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionBinding = DataBindingUtil.setContentView(this, R.layout.activity_permission)
    }

    override fun onResume() {
        super.onResume()
        setupUI()
        getLocation()
    }

    private fun setupUI() {

        permissionBinding.currentLocation.setOnClickListener {
            Permissions.check(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                null,
                object : PermissionHandler() {
                    override fun onGranted() {

                            val request = LocationRequest()
                                .setFastestInterval(1500)
                                .setInterval(300)
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                            builder = LocationSettingsRequest.Builder()
                                .addLocationRequest(request)

                            val result = LocationServices.getSettingsClient(this@PermissionActivity).checkLocationSettings(builder.build())

                            result.addOnCompleteListener {
                                try {
                                    it.getResult(com.google.android.gms.common.api.ApiException::class.java)
                                } catch (e: com.google.android.gms.common.api.ApiException) {
                                    when(e.statusCode){
                                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                            try {
                                                val resolvableApiException = e as ResolvableApiException
                                                resolvableApiException.startResolutionForResult(this@PermissionActivity, REQUEST_CHANGE_CODE)
                                            } catch (ex: ClassCastException) {

                                            }
                                        }
                                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                            val intent = Intent().apply {
                                                action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                            }
                                            startActivityForResult(intent, REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE)
                                        }
                                    }
                                }
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

        permissionBinding.anotherLocation.setOnClickListener {
            startActivity(Intent(this, LocationSetActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
            val manager: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
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
                applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
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
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses[0].featureName + ", " + addresses[0].thoroughfare
            PreferenceProvider(this).saveSharedPreferences("currentLocation", address)
            PreferenceProvider(this).saveSharedPreferences("latitude", location.latitude.toString())
            PreferenceProvider(this).saveSharedPreferences("longitude", location.longitude.toString())
            locationManager.removeUpdates(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("location", address)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
}