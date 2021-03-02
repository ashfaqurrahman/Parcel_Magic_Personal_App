package com.airposted.bitoronbd.ui.location_set

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.LocationSetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationSetViewModel (
    private val repository: LocationSetRepository,
) : ViewModel() {

    private val setOnMap = MutableLiveData(false)

    fun getSetOnMap(): LiveData<Boolean> {
        return  setOnMap
    }

    fun setOnMapTrue() {
        setOnMap.postValue(true)
    }

    fun setOnMapFalse() {
        setOnMap.postValue(false)
    }

    suspend fun getLocations(
        url: String
    ) = withContext(Dispatchers.IO) { repository.locationSearch(url) }

    suspend fun getDirections(
        url: String
    ) = withContext(Dispatchers.IO) { repository.directionSearch(url) }
}