package com.zhengdesheng.z202304100318.ademo.data.api

import com.zhengdesheng.z202304100318.ademo.data.model.ShopResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ShopApi {
    @GET("api/shops/nearby")
    suspend fun getNearbyShops(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radius: Int = 1000,
        @Query("category") category: String? = null
    ): ShopResponse

    @GET("api/shops/search")
    suspend fun searchShops(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ShopResponse
}
