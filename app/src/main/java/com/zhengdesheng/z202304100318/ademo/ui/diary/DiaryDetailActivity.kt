package com.zhengdesheng.z202304100318.ademo.ui.diary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.data.entity.Diary
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop
import com.zhengdesheng.z202304100318.ademo.data.repository.DiaryRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.FoodItemRepository
import com.zhengdesheng.z202304100318.ademo.data.repository.ShopRepository
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityDiaryDetailBinding
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.DiaryViewModel
import com.zhengdesheng.z202304100318.ademo.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class DiaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryDetailBinding
    private lateinit var viewModel: DiaryViewModel
    private var diaryId: Long? = null
    private val imageUris = mutableListOf<Uri>()
    private val shops = mutableListOf<Shop>()

    companion object {
        private const val REQUEST_CAMERA = 1
        private const val REQUEST_GALLERY = 2
        private const val REQUEST_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupUI()
        loadData()
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

        binding.imageViewAdd.setOnClickListener {
            showImagePickerDialog()
        }

        binding.buttonSave.setOnClickListener {
            saveDiary()
        }

        loadShops()
    }

    private fun loadShops() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            database.shopDao().getAllShops().collect { shopList ->
                shops.clear()
                shops.addAll(shopList)
                updateShopSpinner()
            }
        }
    }

    private fun updateShopSpinner() {
        val shopNames = shops.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shopNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerShop.adapter = adapter
    }

    private fun loadData() {
        diaryId = intent.getLongExtra("diary_id", -1)
        if (diaryId != null && diaryId!! > 0) {
            lifecycleScope.launch {
                val diary = viewModel.getDiaryById(diaryId!!)
                diary?.let {
                    binding.editTextTitle.setText(it.title)
                    binding.editTextContent.setText(it.content)
                    binding.editTextTags.setText(it.tags)
                    binding.ratingBar.rating = it.rating

                    val shopIndex = shops.indexOfFirst { shop -> shop.id == it.shopId }
                    if (shopIndex >= 0) {
                        binding.spinnerShop.setSelection(shopIndex)
                    }

                    val imageUrls = it.imageUrls.split(",")
                    imageUrls.forEach { url ->
                        try {
                            val uri = Uri.parse(url)
                            imageUris.add(uri)
                            addImageToLayout(uri)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("拍照", "从相册选择")
        AlertDialog.Builder(this)
            .setTitle("选择图片")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION)
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "需要相机权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val uri = data.data
            uri?.let {
                imageUris.add(it)
                addImageToLayout(it)
            }
        }
    }

    private fun addImageToLayout(uri: Uri) {
        val imageView = ImageView(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(100, 100)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
            setOnClickListener {
                showImageDialog(uri)
            }
        }
        binding.linearLayoutImages.addView(imageView, binding.linearLayoutImages.childCount - 1)
    }

    private fun showImageDialog(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("操作")
            .setItems(arrayOf("查看", "删除")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    }
                    1 -> {
                        imageUris.remove(uri)
                        binding.linearLayoutImages.removeAllViews()
                        binding.linearLayoutImages.addView(binding.imageViewAdd)
                        imageUris.forEach { addImageToLayout(it) }
                    }
                }
            }
            .show()
    }

    private fun saveDiary() {
        val title = binding.editTextTitle.text.toString().trim()
        val content = binding.editTextContent.text.toString().trim()
        val tags = binding.editTextTags.text.toString().trim()
        val rating = binding.ratingBar.rating
        val selectedShopPosition = binding.spinnerShop.selectedItemPosition

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedShopPosition < 0 || selectedShopPosition >= shops.size) {
            Toast.makeText(this, "请先添加店铺", Toast.LENGTH_SHORT).show()
            return
        }

        val shopId = shops[selectedShopPosition].id
        val imageUrls = imageUris.joinToString(",") { it.toString() }

        val diary = Diary(
            id = diaryId ?: 0,
            shopId = shopId,
            title = title,
            content = content,
            rating = rating,
            imageUrls = imageUrls,
            tags = tags
        )

        lifecycleScope.launch {
            if (diaryId != null && diaryId!! > 0) {
                viewModel.updateDiary(diary)
                Toast.makeText(this@DiaryDetailActivity, "更新成功", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addDiary(diary)
                Toast.makeText(this@DiaryDetailActivity, "保存成功", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
