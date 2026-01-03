package com.zhengdesheng.z202304100318.ademo.data.network

import com.zhengdesheng.z202304100318.ademo.data.model.ShopResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ShopApiService {
    
    @GET("shops/nearby")
    fun getNearbyShops(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 2000,
        @Query("category") category: String? = null
    ): Call<ShopResponse>
    
    @GET("shops/recommended")
    fun getRecommendedShops(
        @Query("limit") limit: Int = 10
    ): Call<ShopResponse>
    
    @GET("shops/search")
    fun searchShops(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Call<ShopResponse>
}
