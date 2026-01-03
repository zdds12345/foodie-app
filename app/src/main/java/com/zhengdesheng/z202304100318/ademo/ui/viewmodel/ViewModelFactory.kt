package com.zhengdesheng.z202304100318.ademo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zhengdesheng.z202304100318.ademo.data.repository.DiaryRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.FoodItemRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.ShopRepository

class ViewModelFactory(
    private val shopRepository: ShopRepository,
    private val diaryRepository: DiaryRepository,
    private val foodItemRepository: FoodItemRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ShopViewModel::class.java -> ShopViewModel(shopRepository) as T
            DiaryViewModel::class.java -> DiaryViewModel(diaryRepository) as T
            WheelViewModel::class.java -> WheelViewModel(foodItemRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
