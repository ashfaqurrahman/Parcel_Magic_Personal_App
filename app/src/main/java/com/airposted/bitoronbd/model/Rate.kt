package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class Rate(
    @SerializedName("base_price_personal")
    val basePricePersonal: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("merchant_inside_dhaka")
    val merchantInsideDhaka: Int,
    @SerializedName("merchant_outside_dhaka")
    val merchantOutsideDhaka: Int,
    @SerializedName("per_km_price")
    val perKmPrice: Int,
    @SerializedName("weight")
    val weight: Int,
    @SerializedName("width")
    val width: Int
)