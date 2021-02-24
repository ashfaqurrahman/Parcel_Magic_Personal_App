package com.airposted.bitoronbd.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.data.network.preferences.PreferenceProvider
import com.airposted.bitoronbd.model.SearchLocation
import com.airposted.bitoronbd.model.SettingModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeRepository(context: Context, private val api: MyApi): SafeApiRequest() {
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

    suspend fun getSetting(): SettingModel {
        return apiRequest { api.getSetting() }
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