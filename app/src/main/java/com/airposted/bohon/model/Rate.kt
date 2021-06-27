package com.airposted.bohon.model


import com.google.gson.annotations.SerializedName

data class Rate(
    @SerializedName("base_price_express")
    val basePriceExpress: Int,
    @SerializedName("base_price_quick")
    val basePriceQuick: Int,
    @SerializedName("flat_cancel_fee")
    val flatCancelFee: Int,
    @SerializedName("flat_refund_fee")
    val flatRefundFee: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("Length")
    val length: Double,
    @SerializedName("logistics_fee")
    val logisticsFee: Int,
    @SerializedName("merchant_inside_dhaka")
    val merchantInsideDhaka: Int,
    @SerializedName("merchant_outside_dhaka")
    val merchantOutsideDhaka: Int,
    @SerializedName("package_name")
    val packageName: String,
    @SerializedName("per_km_price_quick")
    val perKmPriceQuick: Int,
    @SerializedName("per_km_price_express")
    val perKmPriceExpress: Int,
    @SerializedName("weight")
    val weight: Int,
    @SerializedName("width")
    val width: Int
)