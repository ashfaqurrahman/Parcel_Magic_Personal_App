package com.airposted.bohon.model.auth

data class User(
    val active: Int,
    val created_at: String,
    val email: Any,
    val fcm_token_driver: Any,
    val fcm_token_personal: Any,
    val id: Int,
    val image: String,
    val password: String,
    val phone: String,
    val pin: Any,
    val role: Int,
    val updated_at: Any,
    val username: String
)