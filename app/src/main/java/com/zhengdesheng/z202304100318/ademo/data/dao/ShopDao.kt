package com.zhengdesheng.z202304100318.ademo.data.dao

import androidx.room.*
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops ORDER BY createdAt DESC")
    fun getAllShops(): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE id = :shopId")
    suspend fun getShopById(shopId: Long): Shop?

    @Query("SELECT * FROM shops WHERE category = :category ORDER BY createdAt DESC")
    fun getShopsByCategory(category: String): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE isVisited = 1 ORDER BY createdAt DESC")
    fun getVisitedShops(): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchShops(query: String): Flow<List<Shop>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: Shop): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShops(shops: List<Shop>)

    @Update
    suspend fun updateShop(shop: Shop)

    @Delete
    suspend fun deleteShop(shop: Shop)

    @Query("DELETE FROM shops WHERE id = :shopId")
    suspend fun deleteShopById(shopId: Long)

    @Query("DELETE FROM shops")
    suspend fun deleteAllShops()
}
