package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class LocationDetails(
    @SerializedName("result")
    val result: Result
)