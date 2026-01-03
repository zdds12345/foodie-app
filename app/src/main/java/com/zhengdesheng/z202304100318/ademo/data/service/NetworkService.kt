package com.zhengdesheng.z202304100318.ademo.data.service

import android.content.Context
import com.google.gson.Gson
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.api.RetrofitClient
import com.zhengdesheng.z202304100318.ademo.data.model.RecipeResponse
import com.zhengdesheng.z202304100318.ademo.data.model.RecipeSearchRequest
import com.zhengdesheng.z202304100318.ademo.data.model.ShopResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object NetworkService {
    private val gson = Gson()

    // 获取附近商家列表（使用模拟数据）
    suspend fun getNearbyShops(useMock: Boolean = true, lat: Double, lng: Double, radius: Int = 1000): ShopResponse {
        return withContext(Dispatchers.IO) {
            if (useMock) {
                // 读取模拟数据
                val context = RetrofitClient.applicationContext
                val inputStream = context.resources.openRawResource(R.raw.shops_mock_data)
                val reader = InputStreamReader(inputStream)
                val shopResponse = gson.fromJson(reader, ShopResponse::class.java)
                reader.close()
                inputStream.close()
                shopResponse
            } else {
                // 真实API请求
                RetrofitClient.shopApi.getNearbyShops(lat, lng, radius)
            }
        }
    }

    // 搜索商家（使用模拟数据）
    suspend fun searchShops(useMock: Boolean = true, keyword: String): ShopResponse {
        return withContext(Dispatchers.IO) {
            if (useMock) {
                // 读取模拟数据并过滤
                val context = RetrofitClient.applicationContext
                val inputStream = context.resources.openRawResource(R.raw.shops_mock_data)
                val reader = InputStreamReader(inputStream)
                val shopResponse = gson.fromJson(reader, ShopResponse::class.java)
                reader.close()
                inputStream.close()

                // 过滤出包含关键词的商家
                val filteredData = shopResponse.data.filter { shop ->
                    shop.name.contains(keyword, ignoreCase = true) ||
                            shop.category.contains(keyword, ignoreCase = true) ||
                            shop.address.contains(keyword, ignoreCase = true)
                }

                shopResponse.copy(data = filteredData)
            } else {
                // 真实API请求
                RetrofitClient.shopApi.searchShops(keyword)
            }
        }
    }

    // 根据食材获取推荐食谱（使用模拟数据）
    suspend fun getRecommendedRecipes(useMock: Boolean = true, ingredients: List<String>): RecipeResponse {
        return withContext(Dispatchers.IO) {
            if (useMock) {
                // 读取模拟食谱数据
                val context = RetrofitClient.applicationContext
                val inputStream = context.resources.openRawResource(R.raw.recipes_mock_data)
                val reader = InputStreamReader(inputStream)
                val recipeResponse = gson.fromJson(reader, RecipeResponse::class.java)
                reader.close()
                inputStream.close()

                // 根据食材过滤食谱（只要包含任意一个输入的食材即可）
                val filteredData = recipeResponse.data.filter { recipe ->
                    ingredients.any { ingredient ->
                        recipe.ingredients.any { recipeIngredient ->
                            recipeIngredient.contains(ingredient, ignoreCase = true)
                        }
                    }
                }

                recipeResponse.copy(data = filteredData)
            } else {
                // 真实API请求 - 将食材列表转换为逗号分隔的字符串
                val ingredientsString = ingredients.joinToString(separator = ",")
                RetrofitClient.recipeApi.getRecipesByIngredients(ingredientsString)
            }
        }
    }
}
