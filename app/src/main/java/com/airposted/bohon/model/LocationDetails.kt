package com.airposted.bohon.model


import com.google.gson.annotations.SerializedName

data class LocationDetails(
    @SerializedName("result")
    val result: Result
)