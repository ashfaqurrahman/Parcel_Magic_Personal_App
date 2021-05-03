package com.airposted.bitoronbd.model

data class DataX(
    val coc: String,
    val cod: String,
    val current_status: Int,
    val current_status_msg: String,
    val delivery_charge: Double,
    val id: Int,
    val invoice_no: String,
    val logistics_charge: Double,
    val logistics_tracking: List<Any>,
    val logistics_user_info: List<Any>,
    val order_date: String,
    val personal_order_type: String,
    val ssl_transaction_id: String,
    val who_will_pay: String
)