package com.zhengdesheng.z202304100318.ademo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityMainBinding
import com.zhengdesheng.z202304100318.ademo.ui.diary.DiaryListActivity
import com.zhengdesheng.z202304100318.ademo.ui.map.MapActivity
import com.zhengdesheng.z202304100318.ademo.ui.profile.ProfileActivity
import com.zhengdesheng.z202304100318.ademo.ui.recipe.RecipeListActivity
import com.zhengdesheng.z202304100318.ademo.ui.shop.ShopListActivity
import com.zhengdesheng.z202304100318.ademo.ui.wheel.WheelActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        binding.cardWheel.setOnClickListener {
            Log.d("MainActivity", "Wheel clicked")
            startActivity(Intent(this, WheelActivity::class.java))
        }

        binding.cardShops.setOnClickListener {
            Log.d("MainActivity", "Shops clicked")
            startActivity(Intent(this, ShopListActivity::class.java))
        }

        binding.cardDiaries.setOnClickListener {
            Log.d("MainActivity", "Diaries clicked")
            startActivity(Intent(this, DiaryListActivity::class.java))
        }

        binding.cardMap.setOnClickListener {
            Log.d("MainActivity", "Map clicked - starting MapActivity")
            try {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
                Log.d("MainActivity", "MapActivity started successfully")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to start MapActivity", e)
            }
        }

        binding.cardRecipes.setOnClickListener {
            Log.d("MainActivity", "Recipes clicked - starting RecipeListActivity")
            try {
                val intent = Intent(this, RecipeListActivity::class.java)
                startActivity(intent)
                Log.d("MainActivity", "RecipeListActivity started successfully")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to start RecipeListActivity", e)
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_shops -> {
                    startActivity(Intent(this, ShopListActivity::class.java))
                    true
                }
                R.id.navigation_diaries -> {
                    startActivity(Intent(this, DiaryListActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.navigation_recipes -> {
                    startActivity(Intent(this, RecipeListActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
