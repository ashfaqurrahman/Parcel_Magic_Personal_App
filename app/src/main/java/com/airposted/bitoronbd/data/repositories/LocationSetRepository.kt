package com.airposted.bitoronbd.data.repositories

import com.airposted.bitoronbd.data.network.MyApi
import com.airposted.bitoronbd.data.network.SafeApiRequest
import com.airposted.bitoronbd.model.SearchLocation

class LocationSetRepository (
    private val api: MyApi,
) : SafeApiRequest() {

    suspend fun locationSearch(mobile: String): SearchLocation {
        return apiRequest { api.getPlacesNameList(mobile) }
    }
}