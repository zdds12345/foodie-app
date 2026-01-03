package com.zhengdesheng.z202304100318.ademo.data.api

import com.zhengdesheng.z202304100318.ademo.data.model.RecipeResponse
import com.zhengdesheng.z202304100318.ademo.data.model.ShopResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.spoonacular.com/"
    
    lateinit var applicationContext: android.content.Context
        private set
    
    fun initialize(context: android.content.Context) {
        applicationContext = context.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val apiKeyInterceptor = Interceptor {
        val original = it.request()
        val originalHttpUrl = original.url
        
        val url = originalHttpUrl.newBuilder()
            .addQueryParameter("apiKey", "your-api-key-here")
            .build()
        
        val requestBuilder = original.newBuilder()
            .url(url)
        
        val request = requestBuilder.build()
        it.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(apiKeyInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val shopApi: ShopApi = retrofit.create(ShopApi::class.java)
    val recipeApi: RecipeApi = retrofit.create(RecipeApi::class.java)
}
