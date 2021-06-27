package com.airposted.bohon.model


import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("location")
    val location: LocationLatLong
)