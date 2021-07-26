package com.airposted.bohon.model.auth

data class AuthResponse(
    val `data`: Data,
    val msg: String,
    val status: Int,
    val success: Boolean,
    val user: User
)