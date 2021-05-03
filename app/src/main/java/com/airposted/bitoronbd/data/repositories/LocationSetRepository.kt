package com.airposted.bitoronbd.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.data.network.responses.SetParcelResponse
import com.airposted.bitoronbd.model.*

class LocationSetRepository (
    context: Context,
    private val api: MyApi,
) : SafeApiRequest() {
    private val appContext = context.applicationContext

    private val from = MutableLiveData<LocationDetailsWithName>()
    private val to = MutableLiveData<LocationDetailsWithName>()

    init {
        from
    }

    fun getFromAddress(): LiveData<LocationDetailsWithName> {
        return from
    }

    fun getToAddress(): LiveData<LocationDetailsWithName> {
        return to
    }

    suspend fun locationSearch(url: String): SearchLocation {
        return apiRequest { api.getPlacesNameList(url)}
    }

    suspend fun directionSearch(url: String): GoogleMapDTO {
        return apiRequest { api.getDirectionsList(url)}
    }

    suspend fun setOrder(
        setParcel: SetParcel
    ) : SetParcelResponse {
        return apiRequest { api.setOrder(
            PersistentUser.getInstance().getAccessToken(
            appContext
        ), setParcel)}
    }
}