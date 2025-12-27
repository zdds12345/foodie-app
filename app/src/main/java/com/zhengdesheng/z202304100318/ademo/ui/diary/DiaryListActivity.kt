package com.zhengdesheng.z202304100318.ademo.ui.diary

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.data.entity.Diary
import com.zhengdesheng.z202304100318.ademo.data.repository.DiaryRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.FoodItemRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.ShopRepository
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityDiaryListBinding
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.DiaryViewModel
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.ViewModelFactory

class DiaryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryListBinding
    private lateinit var viewModel: DiaryViewModel
    private lateinit var adapter: DiaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupUI()
        observeData()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DiaryRepository(database.diaryDao())
        val factory = ViewModelFactory(
            shopRepository = ShopRepository(database.shopDao()),
            diaryRepository = repository,
            foodItemRepository = FoodItemRepository(database.foodItemDao())
        )
        viewModel = ViewModelProvider(this, factory)[DiaryViewModel::class.java]
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = DiaryAdapter(
            onItemClick = { diary ->
                val intent = Intent(this, DiaryDetailActivity::class.java)
                intent.putExtra("diary_id", diary.id)
                startActivity(intent)
            },
            onItemLongClick = { diary ->
                showDeleteDialog(diary)
            }
        )

        binding.recyclerViewDiaries.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDiaries.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadDiaries()
        }

        binding.fabAddDiary.setOnClickListener {
            val intent = Intent(this, DiaryDetailActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.diaries.collect { diaries ->
                adapter.submitList(diaries)
                binding.swipeRefreshLayout.isRefreshing = false
                binding.textViewEmpty.visibility = if (diaries.isEmpty()) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@DiaryListActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteDialog(diary: Diary) {
        AlertDialog.Builder(this)
            .setTitle("删除日记")
            .setMessage("确定要删除这条日记吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteDiary(diary)
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
