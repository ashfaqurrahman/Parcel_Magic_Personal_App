package com.airposted.bohon.model

import com.google.gson.annotations.SerializedName

data class Prediction(
    @SerializedName("description")
    val description: String,
    @SerializedName("terms")
    val terms: List<Term>,
    @SerializedName("place_id")
    val placeId: String,
)