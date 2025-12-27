package com.zhengdesheng.z202304100318.ademo.ui.wheel

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.data.entity.FoodItem
import com.zhengdesheng.z202304100318.ademo.data.repository.FoodItemRepository
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityWheelBinding
// import com.zhengdesheng.z202304100318.ademo.native.NativeLib
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.ViewModelFactory
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.WheelViewModel
import kotlinx.coroutines.launch

class WheelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWheelBinding
    private lateinit var viewModel: WheelViewModel
    // private val nativeLib = NativeLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWheelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupUI()
        observeData()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FoodItemRepository(database.foodItemDao())
        val factory = ViewModelFactory(
            shopRepository = com.zhengdesheng.z202304100318.ademo.data.repository.ShopRepository(database.shopDao()),
            diaryRepository = com.zhengdesheng.z202304100318.ademo.data.repository.DiaryRepository(database.diaryDao()),
            foodItemRepository = repository
        )
        viewModel = ViewModelProvider(this, factory)[WheelViewModel::class.java]
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.buttonSpin.setOnClickListener {
            if (viewModel.selectedItems.value.isEmpty()) {
                Toast.makeText(this, "请先添加选项", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // nativeLib.playSpinSound()
            binding.wheelView.spin()
            viewModel.spin()
        }

        binding.buttonManage.setOnClickListener {
            // nativeLib.playClickSound()
            showManageDialog()
        }

        binding.wheelView.setOnWheelResultListener { result ->
            // nativeLib.playWinSound()
            binding.textResult.text = "今天吃：$result"
            binding.textResult.visibility = android.view.View.VISIBLE
            viewModel.setResult(result)
            Toast.makeText(this, "今天吃：$result", Toast.LENGTH_LONG).show()
        }

        lifecycleScope.launch {
            viewModel.selectedItems.collect { items ->
                val itemNames = items.map { it.name }
                binding.wheelView.setItems(itemNames)
            }
        }
    }

    private fun observeData() {
        viewModel.loadSelectedItems()
    }

    private fun showManageDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food_item, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextFoodName)
        val editTextCategory = dialogView.findViewById<EditText>(R.id.editFoodCategory)

        AlertDialog.Builder(this)
            .setTitle("添加美食选项")
            .setView(dialogView)
            .setPositiveButton("添加") { _, _ ->
                val name = editTextName.text.toString().trim()
                val category = editTextCategory.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addFoodItem(name, if (category.isEmpty()) "其他" else category)
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "请输入美食名称", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
