package com.airposted.bitoronbd.model


import com.google.gson.annotations.SerializedName

data class Info(
    @SerializedName("address")
    val address: String,
    @SerializedName("airposted_from_address")
    val airpostedFromAddress: String,
    @SerializedName("application_name")
    val applicationName: String,
    @SerializedName("business_name")
    val businessName: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("contact")
    val contact: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("facebook")
    val facebook: String,
    @SerializedName("google_plus")
    val googlePlus: String,
    @SerializedName("helpline")
    val helpline: String,
    @SerializedName("helpmail")
    val helpmail: String,
    @SerializedName("icon_photo")
    val iconPhoto: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("logo_photo")
    val logoPhoto: String,
    @SerializedName("owners_name")
    val ownersName: String,
    @SerializedName("postcode")
    val postcode: String,
    @SerializedName("store_address")
    val storeAddress: String,
    @SerializedName("twitter")
    val twitter: String,
    @SerializedName("updated_at")
    val updatedAt: String
)