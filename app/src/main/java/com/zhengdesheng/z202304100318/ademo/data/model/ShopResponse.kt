package com.zhengdesheng.z202304100318.ademo.data.model

import com.google.gson.annotations.SerializedName

data class ShopResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<ShopData>
)

data class ShopData(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("rating")
    val rating: Float,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("businessHours")
    val businessHours: String?
)
