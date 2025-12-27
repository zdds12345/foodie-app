package com.zhengdesheng.z202304100318.ademo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Float,
    val imageUrl: String? = null,
    val phone: String? = null,
    val businessHours: String? = null,
    val isVisited: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
