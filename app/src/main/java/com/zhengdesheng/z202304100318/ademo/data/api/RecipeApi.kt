package com.zhengdesheng.z202304100318.ademo.data.api

import com.zhengdesheng.z202304100318.ademo.data.model.RecipeResponse
import com.zhengdesheng.z202304100318.ademo.data.model.RecipeSearchRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RecipeApi {
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("cuisine") cuisine: String? = null,
        @Query("diet") diet: String? = null,
        @Query("intolerances") intolerances: String? = null,
        @Query("number") number: Int = 10,
        @Query("offset") offset: Int = 0
    ): RecipeResponse

    @GET("recipes/findByIngredients")
    suspend fun getRecipesByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 10,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true
    ): RecipeResponse
}
