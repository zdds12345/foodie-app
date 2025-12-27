package com.zhengdesheng.z202304100318.ademo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zhengdesheng.z202304100318.ademo.data.dao.DiaryDao
import com.zhengdesheng.z202304100318.ademo.data.dao.FoodItemDao
import com.zhengdesheng.z202304100318.ademo.data.dao.ShopDao
import com.zhengdesheng.z202304100318.ademo.data.entity.Diary
import com.zhengdesheng.z202304100318.ademo.data.entity.FoodItem
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop

@Database(
    entities = [Shop::class, Diary::class, FoodItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao
    abstract fun diaryDao(): DiaryDao
    abstract fun foodItemDao(): FoodItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodie_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
