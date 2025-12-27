package com.zhengdesheng.z202304100318.ademo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop
import com.zhengdesheng.z202304100318.ademo.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: ShopRepository) : ViewModel() {

    private val _shops = MutableStateFlow<List<Shop>>(emptyList())
    val shops: StateFlow<List<Shop>> = _shops.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadShops()
    }

    fun loadShops() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.getAllShops().collect { shopList ->
                    _shops.value = shopList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun searchShops(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.searchShops(query).collect { shopList ->
                    _shops.value = shopList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun addShop(shop: Shop) {
        viewModelScope.launch {
            try {
                repository.insertShop(shop)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateShop(shop: Shop) {
        viewModelScope.launch {
            try {
                repository.updateShop(shop)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteShop(shop: Shop) {
        viewModelScope.launch {
            try {
                repository.deleteShop(shop)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    suspend fun getShopById(shopId: Long): Shop? {
        return repository.getShopById(shopId)
    }
}
