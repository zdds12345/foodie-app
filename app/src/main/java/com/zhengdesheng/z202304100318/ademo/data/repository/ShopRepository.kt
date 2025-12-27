package com.zhengdesheng.z202304100318.ademo.data.repository

import com.zhengdesheng.z202304100318.ademo.data.dao.ShopDao
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val shopDao: ShopDao) {

    fun getAllShops(): Flow<List<Shop>> = shopDao.getAllShops()

    suspend fun getShopById(shopId: Long): Shop? = shopDao.getShopById(shopId)

    fun getShopsByCategory(category: String): Flow<List<Shop>> = shopDao.getShopsByCategory(category)

    fun getVisitedShops(): Flow<List<Shop>> = shopDao.getVisitedShops()

    fun searchShops(query: String): Flow<List<Shop>> = shopDao.searchShops(query)

    suspend fun insertShop(shop: Shop): Long = shopDao.insertShop(shop)

    suspend fun insertShops(shops: List<Shop>) = shopDao.insertShops(shops)

    suspend fun updateShop(shop: Shop) = shopDao.updateShop(shop)

    suspend fun deleteShop(shop: Shop) = shopDao.deleteShop(shop)

    suspend fun deleteShopById(shopId: Long) = shopDao.deleteShopById(shopId)

    suspend fun deleteAllShops() = shopDao.deleteAllShops()
}
