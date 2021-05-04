package com.airposted.bitoronbd.model

data class DataX(
    val coc: String,
    val cod: String,
    val current_status: Int,
    val current_status_msg: String,
    val delivery_charge: Int,
    val id: Int,
    val invoice_no: String,
    val logistics_charge: Double,
    val logistics_tracking: LogisticsTracking?,
    val logistics_user_info: LogisticsUserInfo?,
    val order_date: String,
    val personal_order_type: String,
    val ssl_transaction_id: String,
    val who_will_pay: String
)