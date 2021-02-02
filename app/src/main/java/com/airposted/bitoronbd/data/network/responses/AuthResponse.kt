package com.airposted.bitoronbd.data.network.responses

import com.airposted.bitoronbd.model.Data
import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Int
)