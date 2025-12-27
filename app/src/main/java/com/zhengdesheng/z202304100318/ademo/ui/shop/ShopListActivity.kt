package com.zhengdesheng.z202304100318.ademo.ui.shop

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop
import com.zhengdesheng.z202304100318.ademo.data.repository.DiaryRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.FoodItemRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.ShopRepository
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityShopListBinding
import com.zhengdesheng.z202304100318.ademo.ui.map.MapActivity
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.ShopViewModel
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class ShopListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopListBinding
    private lateinit var viewModel: ShopViewModel
    private lateinit var adapter: ShopAdapter

    private val categories = listOf("全部", "中餐", "西餐", "日韩料理", "快餐", "火锅", "烧烤", "甜品", "饮品")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupUI()
        observeData()
        loadSampleData()
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

        adapter = ShopAdapter { shop ->
            showShopDetailDialog(shop)
        }

        binding.recyclerViewShops.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewShops.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadShops()
        }

        binding.fabAddShop.setOnClickListener {
            showAddShopDialog()
        }

        setupTabs()
    }

    private fun setupTabs() {
        categories.forEach { category ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(category))
        }

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                val category = tab?.text.toString()
                if (category == "全部") {
                    viewModel.loadShops()
                } else {
                    lifecycleScope.launch {
                        viewModel.shops.collect { shops ->
                            adapter.submitList(shops.filter { it.category == category })
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.shops.collect { shops ->
                adapter.submitList(shops)
                binding.swipeRefreshLayout.isRefreshing = false
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
                    Toast.makeText(this@ShopListActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadSampleData() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            val shopDao = database.shopDao()
            
            val sampleShops = listOf(
                Shop(
                    name = "杭州片儿川",
                    category = "中餐",
                    address = "杭州市上城区河坊街88号",
                    latitude = 30.2741,
                    longitude = 120.1551,
                    rating = 4.5f,
                    imageUrl = null,
                    phone = "0571-12345678",
                    businessHours = "09:00-22:00"
                ),
                Shop(
                    name = "星巴克咖啡",
                    category = "饮品",
                    address = "杭州市西湖区湖滨路19号",
                    latitude = 30.2592,
                    longitude = 120.1698,
                    rating = 4.3f,
                    imageUrl = null,
                    phone = "0571-87654321",
                    businessHours = "07:00-23:00"
                ),
                Shop(
                    name = "海底捞火锅",
                    category = "火锅",
                    address = "杭州市拱墅区武林广场1号",
                    latitude = 30.2785,
                    longitude = 120.1532,
                    rating = 4.8f,
                    imageUrl = null,
                    phone = "0571-11111111",
                    businessHours = "10:00-03:00"
                ),
                Shop(
                    name = "麦当劳",
                    category = "快餐",
                    address = "杭州市上城区延安路255号",
                    latitude = 30.2588,
                    longitude = 120.1654,
                    rating = 4.0f,
                    imageUrl = null,
                    phone = "0571-22222222",
                    businessHours = "24小时"
                ),
                Shop(
                    name = "寿司之神",
                    category = "日韩料理",
                    address = "杭州市西湖区银泰百货",
                    latitude = 30.2615,
                    longitude = 120.1712,
                    rating = 4.6f,
                    imageUrl = null,
                    phone = "0571-33333333",
                    businessHours = "11:00-22:00"
                )
            )
            
            shopDao.deleteAllShops()
            shopDao.insertShops(sampleShops)
        }
    }

    private fun showShopDetailDialog(shop: Shop) {
        AlertDialog.Builder(this)
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
                    putExtra("shop_id", shop.id)
                    putExtra("shop_name", shop.name)
                }
                startActivity(intent)
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun showAddShopDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_shop, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextShopName)
        val editTextCategory = dialogView.findViewById<EditText>(R.id.editTextCategory)
        val editTextAddress = dialogView.findViewById<EditText>(R.id.editTextAddress)
        val editTextRating = dialogView.findViewById<EditText>(R.id.editTextRating)

        AlertDialog.Builder(this)
            .setTitle("添加店铺")
            .setView(dialogView)
            .setPositiveButton("添加") { _, _ ->
                val name = editTextName.text.toString().trim()
                val category = editTextCategory.text.toString().trim()
                val address = editTextAddress.text.toString().trim()
                val rating = editTextRating.text.toString().toFloatOrNull() ?: 0f

                if (name.isNotEmpty() && address.isNotEmpty()) {
                    val shop = Shop(
                        name = name,
                        category = if (category.isEmpty()) "其他" else category,
                        address = address,
                        latitude = 30.2741,
                        longitude = 120.1551,
                        rating = if (rating > 0) rating else 4.0f
                    )
                    viewModel.addShop(shop)
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
