package com.zhengdesheng.z202304100318.ademo.data.model

import com.google.gson.annotations.SerializedName

data class RecipeResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<RecipeData>
)

data class RecipeData(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("prepTimeMinutes")
    val prepTimeMinutes: Int,
    @SerializedName("cookTimeMinutes")
    val cookTimeMinutes: Int,
    @SerializedName("servings")
    val servings: Int,
    @SerializedName("difficulty")
    val difficulty: String,
    @SerializedName("ingredients")
    val ingredients: List<String>,
    @SerializedName("instructions")
    val instructions: List<String>,
    @SerializedName("cuisine")
    val cuisine: String,
    @SerializedName("calories")
    val calories: Int,
    @SerializedName("protein")
    val protein: Int,
    @SerializedName("carbs")
    val carbs: Int,
    @SerializedName("fat")
    val fat: Int
)

data class RecipeSearchRequest(
    @SerializedName("ingredients")
    val ingredients: List<String>,
    @SerializedName("cuisine")
    val cuisine: String? = null,
    @SerializedName("diet")
    val diet: String? = null,
    @SerializedName("excludeIngredients")
    val excludeIngredients: List<String>? = null
)
