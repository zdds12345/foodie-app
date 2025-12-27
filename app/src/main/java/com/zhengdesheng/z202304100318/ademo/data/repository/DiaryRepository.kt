package com.zhengdesheng.z202304100318.ademo.data.repository

import com.zhengdesheng.z202304100318.ademo.data.dao.DiaryDao
import com.zhengdesheng.z202304100318.ademo.data.entity.Diary
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDao: DiaryDao) {

    fun getAllDiaries(): Flow<List<Diary>> = diaryDao.getAllDiaries()

    suspend fun getDiaryById(diaryId: Long): Diary? = diaryDao.getDiaryById(diaryId)

    fun getDiariesByShopId(shopId: Long): Flow<List<Diary>> = diaryDao.getDiariesByShopId(shopId)

    fun searchDiaries(query: String): Flow<List<Diary>> = diaryDao.searchDiaries(query)

    suspend fun insertDiary(diary: Diary): Long = diaryDao.insertDiary(diary)

    suspend fun updateDiary(diary: Diary) = diaryDao.updateDiary(diary)

    suspend fun deleteDiary(diary: Diary) = diaryDao.deleteDiary(diary)

    suspend fun deleteDiaryById(diaryId: Long) = diaryDao.deleteDiaryById(diaryId)

    suspend fun deleteAllDiaries() = diaryDao.deleteAllDiaries()
}
