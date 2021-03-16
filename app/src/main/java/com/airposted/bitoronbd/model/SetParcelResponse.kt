package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class SetParcelResponse(
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("status")
    val status: Int,
    @SerializedName("success")
    val success: Boolean
)