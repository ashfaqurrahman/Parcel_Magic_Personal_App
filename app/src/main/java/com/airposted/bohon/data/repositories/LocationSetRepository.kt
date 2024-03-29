package com.airposted.bohon.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aapbd.appbajarlib.storage.PersistentUser
import com.airposted.bohon.data.network.MyApi
import com.airposted.bohon.data.network.SafeApiRequest
import com.airposted.bohon.data.network.responses.SetParcelResponse
import com.airposted.bohon.model.*
import com.airposted.bohon.model.coupon.CheckCouponModel
import com.airposted.bohon.model.coupon.Coupon
import com.airposted.bohon.model.coupon.Coupons
import com.airposted.bohon.model.rating.RateDeliveryMan

class LocationSetRepository (
    context: Context,
    private val api: MyApi,
) : SafeApiRequest() {
    private val appContext = context.applicationContext

    private val from = MutableLiveData<LocationDetailsWithName>()
    private val to = MutableLiveData<LocationDetailsWithName>()
    val couponPrice = MutableLiveData<CheckCouponModel>()

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

    suspend fun locationDetails(url: String): LocationDetails {
        return apiRequest { api.locationDetails(url)}
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

    suspend fun checkCoupon(coupon: String): CheckCouponModel {
        val response = apiRequest { api.checkCoupon(
            PersistentUser.getInstance().getAccessToken(
                appContext
            ), coupon)
        }
        if (response.coupons != null){
            couponPrice.postValue(response)
        }
        return response
    }

    fun couponPriceUpdate() {
        couponPrice.postValue(null)
    }
}