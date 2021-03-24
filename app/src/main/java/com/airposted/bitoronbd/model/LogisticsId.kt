package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class LogisticsId(
    @SerializedName("id")
    val id: Int,
    @SerializedName("image")
    val image: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("role")
    val role: Int,
    @SerializedName("username")
    val username: String
)