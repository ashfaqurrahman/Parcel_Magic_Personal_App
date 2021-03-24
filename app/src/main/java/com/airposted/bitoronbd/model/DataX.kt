package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class DataX(
    @SerializedName("coc")
    val coc: String,
    @SerializedName("cod")
    val cod: String,
    @SerializedName("curent_status")
    val curentStatus: String,
    @SerializedName("delivery_charge")
    val deliveryCharge: Double,
    @SerializedName("id")
    val id: Int,
    @SerializedName("invoice_no")
    val invoiceNo: String,
    @SerializedName("logistics_charge")
    val logisticsCharge: Double,
    @SerializedName("logistics_id")
    val logisticsId: LogisticsId?,
    @SerializedName("order_date")
    val orderDate: String,
    @SerializedName("personal_order_type")
    val personalOrderType: String,
    @SerializedName("ssl_transaction_id")
    val sslTransactionId: String,
    @SerializedName("who_will_pay")
    val whoWillPay: String
)