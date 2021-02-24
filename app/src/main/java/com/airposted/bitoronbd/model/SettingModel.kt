package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class SettingModel(
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