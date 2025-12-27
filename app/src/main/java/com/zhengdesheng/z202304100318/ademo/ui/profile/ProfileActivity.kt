package com.zhengdesheng.z202304100318.ademo.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        loadStatistics()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.textViewClearData.setOnClickListener {
            showClearDataDialog()
        }

        binding.textViewAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            
            database.diaryDao().getAllDiaries().collect { diaries ->
                binding.textViewDiaryCount.text = diaries.size.toString()
            }

            database.shopDao().getAllShops().collect { shops ->
                binding.textViewShopCount.text = shops.size.toString()
            }

            database.foodItemDao().getAllFoodItems().collect { foodItems ->
                binding.textViewFoodCount.text = foodItems.size.toString()
            }
        }
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("清除所有数据")
            .setMessage("确定要清除所有数据吗？此操作不可恢复。")
            .setPositiveButton("确定") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun clearAllData() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            database.diaryDao().deleteAllDiaries()
            database.shopDao().deleteAllShops()
            database.foodItemDao().deleteAllFoodItems()
            Toast.makeText(this@ProfileActivity, "数据已清除", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("关于")
            .setMessage("吃什么 - 决策与探店社区\n\n版本：1.0.0\n\n一个帮助你决定今天吃什么的应用")
            .setPositiveButton("确定", null)
            .show()
    }
}
