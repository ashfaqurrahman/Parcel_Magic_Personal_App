package com.airposted.bitoronbd.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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