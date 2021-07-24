package com.airposted.bohon.model.coupon

data class Coupon(
    val applicable_for: Int,
    val coupon_category: String,
    val coupon_limit: Int,
    val coupon_text: String,
    val coupon_title: String,
    val created_at: String,
    val delivery_date: String,
    val discount_amount: String,
    val discount_max: String,
    val discount_type: Int,
    val expired_on: String,
    val id: Int,
    val message_type: Int,
    val min_amount: String,
    val published: Int,
    val scheduled: Int,
    val section_group: String,
    val updated_at: String,
    val user_id: Int
)