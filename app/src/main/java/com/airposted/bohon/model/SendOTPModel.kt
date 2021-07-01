package com.airposted.bohon.model

data class SendOTPModel(
    val `data`: SendOTPDataModel,
    val msg: String,
    val status: Int,
    val success: Boolean
)