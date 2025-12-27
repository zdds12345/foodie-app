package com.zhengdesheng.z202304100318.ademo.data.repository

import com.zhengdesheng.z202304100318.ademo.data.dao.FoodItemDao
import com.zhengdesheng.z202304100318.ademo.data.entity.FoodItem
import kotlinx.coroutines.flow.Flow

class FoodItemRepository(private val foodItemDao: FoodItemDao) {

    fun getAllFoodItems(): Flow<List<FoodItem>> = foodItemDao.getAllFoodItems()

    fun getSelectedFoodItems(): Flow<List<FoodItem>> = foodItemDao.getSelectedFoodItems()

    fun getFoodItemsByCategory(category: String): Flow<List<FoodItem>> = foodItemDao.getFoodItemsByCategory(category)

    suspend fun insertFoodItem(foodItem: FoodItem): Long = foodItemDao.insertFoodItem(foodItem)

    suspend fun insertFoodItems(foodItems: List<FoodItem>) = foodItemDao.insertFoodItems(foodItems)

    suspend fun updateFoodItem(foodItem: FoodItem) = foodItemDao.updateFoodItem(foodItem)

    suspend fun deleteFoodItem(foodItem: FoodItem) = foodItemDao.deleteFoodItem(foodItem)

    suspend fun deleteFoodItemById(foodItemId: Long) = foodItemDao.deleteFoodItemById(foodItemId)

    suspend fun deleteAllFoodItems() = foodItemDao.deleteAllFoodItems()

    suspend fun updateSelectedStatus(foodItemId: Long, isSelected: Boolean) = 
        foodItemDao.updateSelectedStatus(foodItemId, isSelected)
}
