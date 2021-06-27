package com.airposted.bohon.model


import com.google.gson.annotations.SerializedName

data class LocationLatLong(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)