package com.airposted.bitoronbd.data.repositories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class HomeRepository(context: Context): SafeApiRequest() {
    private val appContext = context.applicationContext

    private val lat = MutableLiveData<Double>()
    private val long = MutableLiveData<Double>()

    suspend fun getLat(): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            fetchLat()
        }
    }

    suspend fun getLong(): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            fetchLong()
        }
    }

    private fun fetchLat(): LiveData<Double>{
        lat.postValue(PreferenceProvider(appContext).getSharedPreferences("latitude")?.toDouble())
        return lat
    }

    private fun fetchLong(): LiveData<Double>{
        long.postValue(PreferenceProvider(appContext).getSharedPreferences("longitude")?.toDouble())
        return long
    }

}