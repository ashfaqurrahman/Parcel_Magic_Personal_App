package com.airposted.bitoronbd.data.network.responses

import com.airposted.bitoronbd.model.Info
import com.airposted.bitoronbd.model.Rate
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