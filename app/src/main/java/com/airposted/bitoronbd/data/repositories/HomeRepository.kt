package com.airposted.bitoronbd.data.repositories

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.responses.SettingResponse
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.data.network.responses.SetParcelResponse
import com.airposted.bitoronbd.model.LocationDetails
import com.airposted.bitoronbd.model.LocationDetailsWithName
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class HomeRepository(context: Context, private val api: MyApi): SafeApiRequest(),
    LocationListener {
    private var mLocationManager: LocationManager? = null
    private val appContext = context.applicationContext
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private val userName = MutableLiveData<String>()

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
    private val currentLocation = MutableLiveData<LocationDetailsWithName>()

    init {
        mLocationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        //Log.e("aaaaaa", location.latitude.toString())

    }
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            gps.postValue(true)
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
                PreferenceProvider(appContext).saveSharedPreferences("latitude", location.latitude.toString())
                PreferenceProvider(appContext).saveSharedPreferences("longitude", location.longitude.toString())
                val geo = Geocoder(appContext, Locale.getDefault())
                val addresses = geo.getFromLocation(location.latitude, location.longitude, 1);
                if (addresses.isEmpty()) {

                }
                else {
                    gps.postValue(true)
                    currentLocation.postValue(LocationDetailsWithName(addresses[0].featureName + ", " + addresses[0].thoroughfare,location.latitude, location.longitude))
                }
            }
        }
    }

    private fun dismiss() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    fun currentLocation(): LiveData<LocationDetailsWithName> {
        gps.postValue(true)
        locationCallback()
        return currentLocation
    }

    suspend fun getName(): LiveData<String> {
        return withContext(Dispatchers.IO) {
            fetchName()
        }
    }

    private fun fetchName(): LiveData<String> {
        userName.postValue(PersistentUser.getInstance().getFullName(appContext))
        return userName
    }

    suspend fun getSetting(): SettingResponse {
        return apiRequest { api.getSetting() }
    }

    suspend fun saveFcmToken(fcm_token: String): SetParcelResponse {
        return apiRequest { api.saveFcmToken(
            PersistentUser.getInstance().getAccessToken(
                appContext
            ), fcm_token) }
    }

    suspend fun deleteFcmToken(): SetParcelResponse {
        return apiRequest { api.deleteFcmToken(
            PersistentUser.getInstance().getAccessToken(
                appContext
            )) }
    }
}