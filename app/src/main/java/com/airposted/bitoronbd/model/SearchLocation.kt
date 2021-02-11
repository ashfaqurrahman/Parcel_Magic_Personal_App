package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class SearchLocation(
    @SerializedName("predictions")
    val predictions: List<Prediction>,
    @SerializedName("status")
    val status: String
)