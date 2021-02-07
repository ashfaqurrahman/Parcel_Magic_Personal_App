package com.airposted.bitoronbd.data.network.responses

import com.airposted.bitoronbd.model.Data
import com.airposted.bitoronbd.model.User
import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("user")
    val user: User?,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: Int
)