package com.airposted.bitoronbd.model

import com.google.gson.annotations.SerializedName

class User (
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("active")
    val active: Int,
    @SerializedName("image")
    val image: String
)