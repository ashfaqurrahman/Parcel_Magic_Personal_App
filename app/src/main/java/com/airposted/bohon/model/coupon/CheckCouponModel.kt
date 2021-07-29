package com.airposted.bohon.model.coupon

data class CheckCouponModel(
    val coupons: Coupons?,
    val message: String,
    val msg: String,
    val status: Int
)