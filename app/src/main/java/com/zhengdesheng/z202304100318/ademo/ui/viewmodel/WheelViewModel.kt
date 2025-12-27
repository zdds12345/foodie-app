package com.zhengdesheng.z202304100318.ademo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhengdesheng.z202304100318.ademo.data.entity.FoodItem
import com.zhengdesheng.z202304100318.ademo.data.repository.FoodItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WheelViewModel(private val repository: FoodItemRepository) : ViewModel() {

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    private val _selectedItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val selectedItems: StateFlow<List<FoodItem>> = _selectedItems.asStateFlow()

    private val _spinning = MutableStateFlow(false)
    val spinning: StateFlow<Boolean> = _spinning.asStateFlow()

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result.asStateFlow()

    init {
        loadFoodItems()
    }

    fun loadFoodItems() {
        viewModelScope.launch {
            try {
                repository.getAllFoodItems().collect { items ->
                    _foodItems.value = items
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSelectedItems() {
        viewModelScope.launch {
            try {
                repository.getSelectedFoodItems().collect { items ->
                    _selectedItems.value = items
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addFoodItem(name: String, category: String) {
        viewModelScope.launch {
            try {
                val foodItem = FoodItem(
                    name = name,
                    category = category,
                    isSelected = true
                )
                repository.insertFoodItem(foodItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateFoodItemSelection(foodItemId: Long, isSelected: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateSelectedStatus(foodItemId, isSelected)
                loadSelectedItems()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            try {
                repository.deleteFoodItem(foodItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun spin() {
        viewModelScope.launch {
            _spinning.value = true
            _result.value = null
        }
    }

    fun setResult(result: String) {
        _result.value = result
        _spinning.value = false
    }

    fun clearResult() {
        _result.value = null
    }
}
