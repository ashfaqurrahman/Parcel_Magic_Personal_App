package com.airposted.bitoronbd.ui.location_set

import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.LocationSetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationSetViewModel (
    private val repository: LocationSetRepository,
) : ViewModel() {

    suspend fun getLocations(
        mobile: String
    ) = withContext(Dispatchers.IO) { repository.locationSearch(mobile) }
}