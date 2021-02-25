package com.airposted.bitoronbd.data.repositories

import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.model.GoogleMapDTO
import com.airposted.bitoronbd.model.SearchLocation

class LocationSetRepository (
    private val api: MyApi,
) : SafeApiRequest() {

    suspend fun locationSearch(url: String): SearchLocation {
        return apiRequest { api.getPlacesNameList(url)}
    }

    suspend fun directionSearch(url: String): GoogleMapDTO {
        return apiRequest { api.getDirectionsList(url)}
    }
}