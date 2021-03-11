package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("cancelled_date")
    val cancelledDate: Any,
    @SerializedName("collect_amount")
    val collectAmount: Int,
    @SerializedName("created_at")
    val createdAt: Any,
    @SerializedName("curent_status")
    val curentStatus: Int,
    @SerializedName("delivery_charge")
    val deliveryCharge: Int,
    @SerializedName("delivery_date")
    val deliveryDate: Any,
    @SerializedName("id")
    val id: Int,
    @SerializedName("invoice_no")
    val invoiceNo: String,
    @SerializedName("item_des")
    val itemDes: String,
    @SerializedName("item_price")
    val itemPrice: Int,
    @SerializedName("item_qty")
    val itemQty: Int,
    @SerializedName("item_type")
    val itemType: Int,
    @SerializedName("item_weight")
    val itemWeight: Int,
    @SerializedName("logistics_id")
    val logisticsId: Int,
    @SerializedName("order_date")
    val orderDate: Any,
    @SerializedName("order_type")
    val orderType: Int,
    @SerializedName("payment_status")
    val paymentStatus: Int,
    @SerializedName("payment_type")
    val paymentType: Int,
    @SerializedName("personal_delivery_type")
    val personalDeliveryType: Int,
    @SerializedName("personal_parcel_type")
    val personalParcelType: Int,
    @SerializedName("pick_address")
    val pickAddress: String,
    @SerializedName("pick_area")
    val pickArea: String,
    @SerializedName("pick_city")
    val pickCity: String,
    @SerializedName("pick_zone")
    val pickZone: String,
    @SerializedName("pre_order_id")
    val preOrderId: Int,
    @SerializedName("recp_address")
    val recpAddress: String,
    @SerializedName("recp_area")
    val recpArea: String,
    @SerializedName("recp_city")
    val recpCity: String,
    @SerializedName("recp_name")
    val recpName: String,
    @SerializedName("recp_phone")
    val recpPhone: String,
    @SerializedName("recp_zone")
    val recpZone: String,
    @SerializedName("special_instruction")
    val specialInstruction: String,
    @SerializedName("total_price")
    val totalPrice: Int,
    @SerializedName("updated_at")
    val updatedAt: Any,
    @SerializedName("user_id")
    val userId: Int
)