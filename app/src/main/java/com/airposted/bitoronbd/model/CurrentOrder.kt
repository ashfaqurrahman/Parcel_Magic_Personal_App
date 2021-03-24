package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class CurrentOrder(
    @SerializedName("data")
    val `data`: List<DataX>,
    @SerializedName("status")
    val status: Int,
    @SerializedName("success")
    val success: Boolean
)