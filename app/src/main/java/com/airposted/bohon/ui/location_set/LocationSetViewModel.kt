package com.airposted.bohon.ui.location_set

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.airposted.bohon.data.repositories.LocationSetRepository
import com.airposted.bohon.model.SetParcel
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

    suspend fun getLocationDetails(
        url: String
    ) = withContext(Dispatchers.IO) { repository.locationDetails(url) }

    suspend fun getDirections(
        url: String
    ) = withContext(Dispatchers.IO) { repository.directionSearch(url) }

    suspend fun setOrder(
        setParcel: SetParcel
    ) = withContext(Dispatchers.IO) { repository.setOrder(setParcel) }

    val couponPrice = repository.couponPrice
    val couponPriceUpdate = repository.couponPriceUpdate()
    suspend fun checkCoupon(coupon:String) = withContext(Dispatchers.IO) { repository.checkCoupon(coupon) }
}