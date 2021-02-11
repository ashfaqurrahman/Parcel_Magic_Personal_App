package com.airposted.bitoronbd.data.repositories

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.google.android.gms.location.*
import java.util.*

class MainRepository(context: Context): SafeApiRequest(), LocationListener {
    private var mLocationManager: LocationManager? = null
    private val appContext = context.applicationContext
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var mFusedLocationClient: FusedLocationProviderClient? = null

    private val mNetworkDetectReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkInternetConnection()
        }
    }

    private val gps = MutableLiveData<Boolean>()
    private val network = MutableLiveData<Boolean>()
    private val currentLocation = MutableLiveData<String>()

    init {
        mLocationManager = appContext.getSystemService(LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(appContext,
                permissions[0]) ==
            PackageManager.PERMISSION_GRANTED) {
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, this)
            appContext.registerReceiver(
                mNetworkDetectReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    fun location(): LiveData<Boolean> {
        return gps
    }

    fun internet(): LiveData<Boolean> {
        return network
    }

    private fun checkInternetConnection() {
        if (isNetworkAvailable(appContext)) {
            network.postValue(true)
        } else {
            network.postValue(false)
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    override fun onLocationChanged(location: Location) {

    }
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            locationCallback()
        }
    }
    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            gps.postValue(false)
            dismiss()
        }
    }

    private fun locationCallback() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
        mLocationRequest = LocationRequest()

        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback, Looper.myLooper()
        )
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                mLastLocation = location
                val geo = Geocoder(appContext, Locale.getDefault())
                val addresses = geo.getFromLocation(location.latitude, location.longitude, 1);
                if (addresses.isEmpty()) {
                    currentLocation.postValue(appContext.getString(R.string.searching))
                }
                else {
                    gps.postValue(true)
                    currentLocation.postValue(addresses[0].featureName + ", " + addresses[0].thoroughfare)
                }
            }
        }
    }

    private fun dismiss() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    fun currentLocation(): LiveData<String> {
        return currentLocation
    }
}