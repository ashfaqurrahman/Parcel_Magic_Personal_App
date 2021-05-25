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
    val who_will_pay: String,
    val item_type: Int,
    val item_qty: String,
    val distance: Int,
    val recp_name: String,
    val recp_phone: String,
    val recp_address: String,
    val pick_address: String,
    val sender_latitude: Double,
    val sender_longitude: Double,
    val receiver_latitude: Double,
    val receiver_longitude: Double
)