package com.airposted.bitoronbd.model

data class OrderList(
    val `data`: List<DataX>,
    val msg: String,
    val status: Int,
    val success: Boolean
)