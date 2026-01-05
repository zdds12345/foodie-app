package com.zhengdesheng.z202304100318.ademo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "diaries",
    foreignKeys = [
        ForeignKey(
            entity = Shop::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("shopId")]
)
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: Long?,
    val title: String,
    val content: String,
    val rating: Float,
    val imageUrls: String,
    val tags: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
