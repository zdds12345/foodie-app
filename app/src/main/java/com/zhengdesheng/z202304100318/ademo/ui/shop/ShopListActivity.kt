package com.zhengdesheng.z202304100318.ademo.ui.shop

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityShopListBinding
import com.zhengdesheng.z202304100318.ademo.data.network.ShopRepository
import com.zhengdesheng.z202304100318.ademo.ui.map.MapActivity
import kotlinx.coroutines.launch

class ShopListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopListBinding
    private lateinit var recommendedAdapter: RecommendedShopAdapter
    private lateinit var shopRepository: ShopRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        shopRepository = ShopRepository(this)
        setupBasicUI()
        setupRecommendedList()
        loadRecommendedShops()
    }



    private fun setupBasicUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        // Hide unused UI components
        binding.recyclerViewShops.visibility = View.GONE
        binding.tabLayout.visibility = View.GONE
        binding.swipeRefreshLayout.visibility = View.GONE
        binding.fabAddShop.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun setupRecommendedList() {
        recommendedAdapter = RecommendedShopAdapter { shop ->
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("shop_id", shop.id.toLongOrNull() ?: 0)
                putExtra("shop_name", shop.name)
                putExtra("shop_latitude", shop.latitude)
                putExtra("shop_longitude", shop.longitude)
                putExtra("shop_address", shop.address)
            }
            startActivity(intent)
        }
        
        binding.recyclerViewRecommended.apply {
            layoutManager = LinearLayoutManager(this@ShopListActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedAdapter
        }
    }

    private fun loadRecommendedShops() {
        lifecycleScope.launch {
            shopRepository.getRecommendedShops()
                .onSuccess { shops ->
                    android.util.Log.d("ShopListActivity", "加载推荐店铺成功，共 ${shops.size} 家")
                    android.util.Log.d("ShopListActivity", "店铺列表: ${shops.map { it.name }.joinToString(", ")}")
                    recommendedAdapter.submitList(shops)
                }
                .onFailure { error ->
                    android.util.Log.e("ShopListActivity", "加载推荐店铺失败", error)
                }
        }
    }








}
