package com.airposted.bitoronbd.data.repositories

import android.content.Context
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.data.network.responses.SetParcelResponse
import com.airposted.bitoronbd.model.GoogleMapDTO
import com.airposted.bitoronbd.model.SearchLocation
import com.airposted.bitoronbd.model.SetParcel

class LocationSetRepository (
    context: Context,
    private val api: MyApi,
) : SafeApiRequest() {
    private val appContext = context.applicationContext

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