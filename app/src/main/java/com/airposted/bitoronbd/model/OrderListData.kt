package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class OrderListData(
    @SerializedName("response")
    val response: List<Response>,
    @SerializedName("status")
    val status: Int
)