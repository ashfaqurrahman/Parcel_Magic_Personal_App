package com.airposted.bitoronbd.model

data class StatusChangeModel(
    val `data`: List<Any>,
    val msg: String,
    val status: Int,
    val success: Boolean
)