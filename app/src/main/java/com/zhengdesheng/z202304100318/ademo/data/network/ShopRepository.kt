package com.zhengdesheng.z202304100318.ademo.data.network

import android.content.Context
import android.util.Log
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop as ShopEntity
import com.zhengdesheng.z202304100318.ademo.data.model.ShopData
import com.zhengdesheng.z202304100318.ademo.data.model.ShopResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class ShopRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "ShopRepository"
        private const val BASE_URL = "https://api.example.com/"
    }
    
    private val gson = Gson()
    
    suspend fun getRecommendedShops(): Result<List<ShopData>> {
        return withContext(Dispatchers.IO) {
            try {
                // 优先从数据库加载数据
                val databaseResult = getRecommendedShopsFromDatabase()
                if (databaseResult.isSuccess) {
                    return@withContext databaseResult
                }
                
                // 如果数据库中没有数据，回退到加载模拟数据
                Log.d(TAG, "数据库中没有数据，回退到加载模拟数据")
                val shops = loadMockData()
                if (shops != null) {
                    Log.d(TAG, "成功加载推荐店铺数据，共 ${shops.size} 家")
                    Result.success(shops)
                } else {
                    Log.e(TAG, "加载推荐店铺数据失败")
                    Result.failure(Exception("无法加载推荐店铺数据"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载推荐店铺数据异常", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun getNearbyShops(latitude: Double, longitude: Double, radius: Int = 2000): Result<List<ShopData>> {
        return withContext(Dispatchers.IO) {
            try {
                val allShops = loadMockData()
                if (allShops != null) {
                    val nearbyShops = allShops.filter { shop ->
                        val distance = calculateDistance(latitude, longitude, shop.latitude, shop.longitude)
                        distance <= radius
                    }.sortedBy { shop ->
                        calculateDistance(latitude, longitude, shop.latitude, shop.longitude)
                    }
                    
                    Log.d(TAG, "成功加载附近店铺数据，共 ${nearbyShops.size} 家")
                    Result.success(nearbyShops)
                } else {
                    Log.e(TAG, "加载附近店铺数据失败")
                    Result.failure(Exception("无法加载附近店铺数据"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载附近店铺数据异常", e)
                Result.failure(e)
            }
        }
    }
    
    private fun loadMockData(): List<ShopData>? {
        return try {
            val inputStream = context.resources.openRawResource(com.zhengdesheng.z202304100318.ademo.R.raw.shops_mock_data)
            val reader = InputStreamReader(inputStream)
            val response = gson.fromJson(reader, ShopResponse::class.java)
            reader.close()
            inputStream.close()
            
            if (response.code == 200) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取模拟数据文件失败", e)
            null
        }
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c * 1000
    }
    
    fun convertToEntity(shopData: ShopData): ShopEntity {
        return ShopEntity(
            id = shopData.id.toLongOrNull() ?: 0,
            name = shopData.name,
            category = shopData.category,
            address = shopData.address,
            latitude = shopData.latitude,
            longitude = shopData.longitude,
            rating = shopData.rating,
            imageUrl = shopData.imageUrl,
            phone = shopData.phone,
            businessHours = shopData.businessHours
        )
    }
    
    private fun convertToModel(shopEntity: ShopEntity): ShopData {
        return ShopData(
            id = shopEntity.id.toString(),
            name = shopEntity.name,
            category = shopEntity.category,
            address = shopEntity.address,
            latitude = shopEntity.latitude,
            longitude = shopEntity.longitude,
            rating = shopEntity.rating,
            imageUrl = shopEntity.imageUrl,
            phone = shopEntity.phone,
            businessHours = shopEntity.businessHours
        )
    }
    
    suspend fun getRecommendedShopsFromDatabase(): Result<List<ShopData>> {
        return withContext(Dispatchers.IO) {
            try {
                val shopDao = AppDatabase.getDatabase(context).shopDao()
                val shopEntities = shopDao.getAllShops().first()
                
                if (shopEntities.isNotEmpty()) {
                    Log.d(TAG, "从数据库加载推荐店铺数据，共 ${shopEntities.size} 家")
                    val shopDataList = shopEntities.map { convertToModel(it) }
                    Result.success(shopDataList)
                } else {
                    Log.d(TAG, "数据库中没有店铺数据")
                    Result.failure(Exception("数据库中没有店铺数据"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "从数据库加载推荐店铺数据异常", e)
                Result.failure(e)
            }
        }
    }
}
