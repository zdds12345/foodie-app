package com.zhengdesheng.z202304100318.ademo.ui.recipe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.api.RetrofitClient
import com.zhengdesheng.z202304100318.ademo.data.service.NetworkService
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityRecipeListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeListBinding
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化RetrofitClient
        RetrofitClient.initialize(applicationContext)

        setupUI()
        setupListeners()
        // 默认使用常见食材推荐食谱
        loadRecommendedRecipes(listOf("鸡蛋", "番茄", "大米"))
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = RecipeAdapter { recipe ->
            showRecipeDetailDialog(recipe)
        }

        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRecipes.adapter = adapter
    }

    private fun setupListeners() {
        binding.buttonSearch.setOnClickListener {
            val ingredientsText = binding.editTextIngredients.text.toString().trim()
            if (ingredientsText.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_ingredients), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 将输入的食材文本分割成列表
            val ingredients = ingredientsText.split("、", ",", " ").filter { it.isNotEmpty() }
            if (ingredients.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_valid_ingredients), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadRecommendedRecipes(ingredients)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            val ingredientsText = binding.editTextIngredients.text.toString().trim()
            val ingredients = if (ingredientsText.isEmpty()) {
                listOf("鸡蛋", "番茄", "大米")
            } else {
                ingredientsText.split("、", ",", " ").filter { it.isNotEmpty() }
            }
            loadRecommendedRecipes(ingredients)
        }
    }

    private fun loadRecommendedRecipes(ingredients: List<String>) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.swipeRefreshLayout.isRefreshing = true

            try {
                // 使用NetworkService获取推荐食谱
                val recommendedRecipes = NetworkService.getRecommendedRecipes(true, ingredients)
                val recipes = recommendedRecipes.data
                adapter.submitList(recipes)
                
                // 显示空数据提示
                if (recipes.isEmpty()) {
                    Toast.makeText(this@RecipeListActivity, getString(R.string.no_recipes_found), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecipeListActivity, "加载推荐食谱失败: ${e.message}", Toast.LENGTH_SHORT).show()
                // 发生错误时显示空数据
                adapter.submitList(emptyList())
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showRecipeDetailDialog(recipe: com.zhengdesheng.z202304100318.ademo.data.model.RecipeData) {
        val ingredients = recipe.ingredients.joinToString(separator = "\n• ", prefix = "\n• ")
        val instructions = recipe.instructions.joinToString(separator = "\n\n", prefix = "\n")

        android.app.AlertDialog.Builder(this)
            .setTitle(recipe.title)
            .setMessage(
                "烹饪时间：${recipe.prepTimeMinutes + recipe.cookTimeMinutes}分钟\n"
                        + "难度：${recipe.difficulty}\n"
                        + "份量：${recipe.servings}人份\n"
                        + "菜系：${recipe.cuisine}\n\n"
                        + "食材：${ingredients}\n\n"
                        + "步骤：${instructions}\n\n"
                        + "营养信息：\n"
                        + "• 卡路里：${recipe.calories}kcal\n"
                        + "• 蛋白质：${recipe.protein}g\n"
                        + "• 碳水化合物：${recipe.carbs}g\n"
                        + "• 脂肪：${recipe.fat}g"
            )
            .setPositiveButton(getString(R.string.close), null)
            .show()
    }
}