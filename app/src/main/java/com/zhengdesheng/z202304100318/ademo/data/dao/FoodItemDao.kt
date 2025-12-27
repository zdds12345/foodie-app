package com.zhengdesheng.z202304100318.ademo.data.dao

import androidx.room.*
import com.zhengdesheng.z202304100318.ademo.data.entity.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items ORDER BY createdAt DESC")
    fun getAllFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isSelected = 1")
    fun getSelectedFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE category = :category ORDER BY createdAt DESC")
    fun getFoodItemsByCategory(category: String): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(foodItems: List<FoodItem>)

    @Update
    suspend fun updateFoodItem(foodItem: FoodItem)

    @Delete
    suspend fun deleteFoodItem(foodItem: FoodItem)

    @Query("DELETE FROM food_items WHERE id = :foodItemId")
    suspend fun deleteFoodItemById(foodItemId: Long)

    @Query("DELETE FROM food_items")
    suspend fun deleteAllFoodItems()

    @Query("UPDATE food_items SET isSelected = :isSelected WHERE id = :foodItemId")
    suspend fun updateSelectedStatus(foodItemId: Long, isSelected: Boolean)
}
