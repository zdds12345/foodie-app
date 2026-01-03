package com.zhengdesheng.z202304100318.ademo.ui.shop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.api.RetrofitClient
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.data.repository.DiaryRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.FoodItemRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.ShopRepository
import com.zhengdesheng.z202304100318.ademo.data.service.NetworkService
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityRecommendedListBinding
import com.zhengdesheng.z202304100318.ademo.ui.map.MapActivity
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.ViewModelFactory
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.ShopViewModel
import kotlinx.coroutines.launch

class RecommendedListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecommendedListBinding
    private lateinit var viewModel: ShopViewModel
    private lateinit var adapter: RecommendedShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendedListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化RetrofitClient
        RetrofitClient.initialize(applicationContext)

        setupViewModel()
        setupUI()
        observeData()
        loadRecommendedShops()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ShopRepository(database.shopDao())
        val factory = ViewModelFactory(
            shopRepository = repository,
            diaryRepository = DiaryRepository(database.diaryDao()),
            foodItemRepository = FoodItemRepository(database.foodItemDao())
        )
        viewModel = ViewModelProvider(this, factory)[ShopViewModel::class.java]
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = RecommendedShopAdapter { shop ->
            showShopDetailDialog(shop)
        }

        binding.recyclerViewRecommended.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecommended.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadRecommendedShops()
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.isLoading.collect {
                binding.progressBar.visibility = if (it) android.view.View.VISIBLE else android.view.View.GONE
                binding.swipeRefreshLayout.isRefreshing = it
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect {
                it?.let {
                    Toast.makeText(this@RecommendedListActivity, it, Toast.LENGTH_SHORT).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun loadRecommendedShops() {
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            try {
                // 使用NetworkService获取推荐商家列表
                // 这里使用杭州的坐标（河坊街附近）
                val recommendedShops = NetworkService.getNearbyShops(true, 30.2741, 120.1551)
                val shops = recommendedShops.data
                adapter.submitList(shops)
                
                // 显示空数据提示
                if (shops.isEmpty()) {
                    Toast.makeText(this@RecommendedListActivity, getString(R.string.no_shops_found), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecommendedListActivity, "加载推荐商家失败: ${e.message}", Toast.LENGTH_SHORT).show()
                // 发生错误时显示空数据
                adapter.submitList(emptyList())
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showShopDetailDialog(shop: com.zhengdesheng.z202304100318.ademo.data.model.ShopData) {
        android.app.AlertDialog.Builder(this)
            .setTitle(shop.name)
            .setMessage(
                "分类：${shop.category}\n" +
                        "地址：${shop.address}\n" +
                        "评分：${shop.rating}\n" +
                        "电话：${shop.phone ?: "暂无"}\n" +
                        "营业时间：${shop.businessHours ?: "暂无"}"
            )
            .setPositiveButton("查看地图") { _, _ ->
                val intent = Intent(this, MapActivity::class.java).apply {
                    putExtra("shop_name", shop.name)
                    putExtra("shop_lat", shop.latitude)
                    putExtra("shop_lng", shop.longitude)
                }
                startActivity(intent)
            }
            .setNegativeButton("关闭", null)
            .show()
    }
}
