package com.airposted.bohon.model.coupon

data class UserCoupon(
    val coupons: List<Coupon>,
    val message: String
)