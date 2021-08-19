package com.airposted.bohon.data.repositories

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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bohon.data.db.AppDatabase
import com.airposted.bohon.data.network.MyApi
import com.airposted.bohon.data.network.responses.SettingResponse
import com.airposted.bohon.data.network.SafeApiRequest
import com.airposted.bohon.data.network.preferences.PreferenceProvider
import com.airposted.bohon.data.network.responses.SetParcelResponse
import com.airposted.bohon.model.LocationDetailsWithName
import com.airposted.bohon.model.auth.AuthResponse
import com.airposted.bohon.model.coupon.UserCoupon
import com.google.android.gms.location.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.*

class HomeRepository(context: Context, private val api: MyApi, private val db: AppDatabase): SafeApiRequest(),
    LocationListener {
    private var mLocationManager: LocationManager? = null
    private val appContext = context.applicationContext
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val userName = MutableLiveData<String>()
    val userImage = MutableLiveData<String>()

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

        getName()
        getImage()
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
                try {
                    val addresses = geo.getFromLocation(location.latitude, location.longitude, 1);
                    if (addresses.isEmpty()) {

                    }
                    else {
                        gps.postValue(true)
                        currentLocation.postValue(LocationDetailsWithName(addresses[0].featureName + ", " + addresses[0].thoroughfare + ", " + addresses[0].subLocality + ", " + addresses[0].locality, location.latitude, location.longitude))
                    }
                } catch (e: InvocationTargetException) {
                    Log.e("aaaaaa", "Location not found")
                } catch (e: RuntimeException) {
                    Log.e("aaaaaa", "Location not found")
                } catch (e: IOException) {
                    Log.e("aaaaaa", "Location not found")
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

    private fun getName() {
        userName.postValue(PersistentUser.getInstance().getFullName(appContext))
    }
    private fun getImage() {
        userImage.postValue(PersistentUser.getInstance().getUserImage(appContext))
    }

    suspend fun userNameUpdate(
        header: String,
        name: String
    ) : String {
        val response = apiRequest{ api.userNameUpdate(header, name)}
        if (response.success){
            PersistentUser.getInstance().setFullname(appContext, response.user.username)
            userName.postValue(response.user.username)
        }
        return response.msg
    }

    suspend fun userImageUpdate(
        header: String,
        photo: MultipartBody.Part,
        photo_name: RequestBody
    ) : AuthResponse {
        val response = apiRequest { api.userImageUpdate(header, photo, photo_name)}
        PersistentUser.getInstance().setUserImage(appContext, response.user.image)
        userImage.postValue(response.user.image)
        return  response
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

    suspend fun getUserBasedCoupons(): UserCoupon {
        return apiRequest { api.getUserBasedCoupons(
            PersistentUser.getInstance().getAccessToken(
                appContext
            )) }
    }

    suspend fun saveAddress(location: com.airposted.bohon.data.db.Location) = db.getRunDao().insertRun(location)

    fun getAllLocations() = db.getRunDao().getAllLocations()

    fun getLastLocation() = db.getRunDao().getLastLocation()
}