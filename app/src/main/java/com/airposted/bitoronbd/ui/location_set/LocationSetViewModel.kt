package com.airposted.bitoronbd.ui.location_set

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.airposted.bitoronbd.data.repositories.LocationSetRepository
import com.airposted.bitoronbd.model.LocationDetails
import com.airposted.bitoronbd.model.LocationDetailsWithName
import com.airposted.bitoronbd.model.SetParcel
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

    val from = repository.getFromAddress()
    val to = repository.getToAddress()

    suspend fun getLocations(
        url: String
    ) = withContext(Dispatchers.IO) { repository.locationSearch(url) }

    suspend fun getDirections(
        url: String
    ) = withContext(Dispatchers.IO) { repository.directionSearch(url) }

    suspend fun setOrder(
        setParcel: SetParcel
    ) = withContext(Dispatchers.IO) { repository.setOrder(setParcel) }
}