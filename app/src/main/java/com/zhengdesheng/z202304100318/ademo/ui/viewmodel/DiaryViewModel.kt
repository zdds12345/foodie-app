package com.zhengdesheng.z202304100318.ademo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhengdesheng.z202304100318.ademo.data.entity.Diary
import com.zhengdesheng.z202304100318.ademo.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiaryViewModel(private val repository: DiaryRepository) : ViewModel() {

    private val _diaries = MutableStateFlow<List<Diary>>(emptyList())
    val diaries: StateFlow<List<Diary>> = _diaries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedDiary = MutableStateFlow<Diary?>(null)
    val selectedDiary: StateFlow<Diary?> = _selectedDiary.asStateFlow()

    init {
        loadDiaries()
    }

    fun loadDiaries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.getAllDiaries().collect { diaryList ->
                    _diaries.value = diaryList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun searchDiaries(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.searchDiaries(query).collect { diaryList ->
                    _diaries.value = diaryList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun addDiary(diary: Diary) {
        viewModelScope.launch {
            try {
                repository.insertDiary(diary)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    suspend fun addDiaryAndGetId(diary: Diary): Long {
        return try {
            val insertedId = repository.insertDiary(diary)
            android.util.Log.d("DiaryViewModel", "插入日记成功 - ID: $insertedId")
            insertedId
        } catch (e: Exception) {
            android.util.Log.e("DiaryViewModel", "插入日记失败", e)
            _error.value = e.message
            -1L
        }
    }

    fun updateDiary(diary: Diary) {
        viewModelScope.launch {
            try {
                repository.updateDiary(diary)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteDiary(diary: Diary) {
        viewModelScope.launch {
            try {
                repository.deleteDiary(diary)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectDiary(diary: Diary) {
        _selectedDiary.value = diary
    }

    suspend fun getDiaryById(diaryId: Long): Diary? {
        return repository.getDiaryById(diaryId)
    }
}
