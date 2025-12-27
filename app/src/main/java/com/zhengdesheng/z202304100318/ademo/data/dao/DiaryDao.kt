package com.zhengdesheng.z202304100318.ademo.data.dao

import androidx.room.*
import com.zhengdesheng.z202304100318.ademo.data.entity.Diary
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diaries ORDER BY createdAt DESC")
    fun getAllDiaries(): Flow<List<Diary>>

    @Query("SELECT * FROM diaries WHERE id = :diaryId")
    suspend fun getDiaryById(diaryId: Long): Diary?

    @Query("SELECT * FROM diaries WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun getDiariesByShopId(shopId: Long): Flow<List<Diary>>

    @Query("SELECT * FROM diaries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchDiaries(query: String): Flow<List<Diary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: Diary): Long

    @Update
    suspend fun updateDiary(diary: Diary)

    @Delete
    suspend fun deleteDiary(diary: Diary)

    @Query("DELETE FROM diaries WHERE id = :diaryId")
    suspend fun deleteDiaryById(diaryId: Long)

    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()
}
