package com.airposted.bohon.data.network.responses

import com.airposted.bohon.model.Info
import com.airposted.bohon.model.Rate
import com.google.gson.annotations.SerializedName

data class SettingResponse(
    @SerializedName("info")
    val info: Info,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("rate")
    val rate: Rate,
    @SerializedName("status")
    val status: Int,
    @SerializedName("success")
    val success: Boolean
)