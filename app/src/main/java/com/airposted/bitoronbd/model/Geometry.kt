package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("location")
    val location: LocationLatLong
)