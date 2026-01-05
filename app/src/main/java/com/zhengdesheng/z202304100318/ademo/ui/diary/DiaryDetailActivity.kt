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
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiaryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryDetailBinding
    private lateinit var viewModel: DiaryViewModel
    private var diaryId: Long? = null
    private val imageUris = mutableListOf<Uri>()
    private val shops = mutableListOf<Shop>()

    private val cameraLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // 首先检查data中的URI
            val uriFromData = result.data?.data
            if (uriFromData != null) {
                imageUris.add(uriFromData)
                addImageToLayout(uriFromData)
            } else {
                // 如果data中的URI为null，尝试从extra中获取
                val bitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
                bitmap?.let {
                    // 将bitmap保存为临时文件并获取URI
                    val uri = saveBitmapToUri(it)
                    uri?.let {
                        imageUris.add(it)
                        addImageToLayout(it)
                    }
                }
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val uri = result.data?.data
            uri?.let {
                try {
                    // 将相册图片复制到应用私有目录，确保持久化访问
                    val contentResolver = this.contentResolver
                    val inputStream = contentResolver.openInputStream(it)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    
                    // 保存到应用私有目录
                    val savedUri = saveBitmapToUri(bitmap)
                    savedUri?.let {
                        imageUris.add(it)
                        addImageToLayout(it)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DiaryDetailActivity", "复制相册图片失败", e)
                    // 如果复制失败，直接使用原始URI
                    imageUris.add(uri)
                    addImageToLayout(uri)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "需要相机权限", Toast.LENGTH_SHORT).show()
        }
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

        binding.buttonEdit.setOnClickListener {
            setViewMode(true)
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
                android.util.Log.d("DiaryDetailActivity", "店铺列表已更新 - 数量: ${shops.size}")
                // 店铺加载完成后重新加载日记数据
                loadData()
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

                    // 设置店铺选择器
                    val shopIndex = if (it.shopId != null) {
                        shops.indexOfFirst { shop -> shop.id == it.shopId }
                    } else {
                        -1
                    }
                    if (shopIndex >= 0) {
                        binding.spinnerShop.setSelection(shopIndex)
                    } else {
                        // 如果找不到店铺，使用默认选择
                        if (shops.isNotEmpty()) {
                            binding.spinnerShop.setSelection(0)
                        }
                    }

                    // 清空之前的图片列表和视图
                    imageUris.clear()
                    // 保留添加图片按钮，移除其他所有图片视图
                    val childCount = binding.linearLayoutImages.childCount
                    for (i in childCount - 2 downTo 0) {
                        binding.linearLayoutImages.removeViewAt(i)
                    }
                    
                    // 加载图片，确保即使有异常也能切换到查看模式
                    android.util.Log.d("DiaryDetailActivity", "开始加载图片 - imageUrls: ${it.imageUrls}")
                    val imageUrls = if (it.imageUrls.isNotEmpty()) {
                        it.imageUrls.split(",").filter { url -> url.isNotEmpty() }
                    } else {
                        emptyList()
                    }
                    
                    android.util.Log.d("DiaryDetailActivity", "解析后的图片URL数量: ${imageUrls.size}")
                    imageUrls.forEach { url ->
                        try {
                            android.util.Log.d("DiaryDetailActivity", "处理图片URL: $url")
                            val uri = Uri.parse(url)
                            imageUris.add(uri)
                            addImageToLayout(uri)
                        } catch (e: Exception) {
                            android.util.Log.e("DiaryDetailActivity", "加载图片失败: $url", e)
                        }
                    }

                    // 确保切换到查看模式
                    setViewMode(false)
                } ?: run {
                    // 如果找不到日记，显示错误信息并返回
                    Toast.makeText(this@DiaryDetailActivity, "找不到该日记", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            setViewMode(true)
        }
    }

    private fun setViewMode(isEditMode: Boolean) {
        binding.editTextTitle.isEnabled = isEditMode
        binding.editTextContent.isEnabled = isEditMode
        binding.editTextTags.isEnabled = isEditMode
        binding.spinnerShop.isEnabled = isEditMode
        binding.ratingBar.isClickable = isEditMode
        binding.imageViewAdd.visibility = if (isEditMode) android.view.View.VISIBLE else android.view.View.GONE
        binding.buttonSave.visibility = if (isEditMode) android.view.View.VISIBLE else android.view.View.GONE
        binding.buttonEdit.visibility = if (isEditMode) android.view.View.GONE else android.view.View.VISIBLE
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
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 确保相机应用能够返回结果
        if (intent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(this, "未找到相机应用", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun saveBitmapToUri(bitmap: android.graphics.Bitmap): Uri? {
        try {
            // 创建持久化文件，保存在应用的私有目录中
            val filesDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val imageFile = File(filesDir, "diary_image_${System.currentTimeMillis()}.jpg")
            
            // 将Bitmap保存到文件
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // 使用FileProvider生成content://类型的URI
            return FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            android.util.Log.e("DiaryDetailActivity", "保存图片失败", e)
            return null
        }
    }

    private fun addImageToLayout(uri: Uri) {
        val imageView = ImageView(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(100, 100)
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // 使用ContentResolver确保图片能正确加载
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                setImageBitmap(bitmap)
                android.util.Log.d("DiaryDetailActivity", "图片加载成功: $uri")
            } catch (e: Exception) {
                android.util.Log.e("DiaryDetailActivity", "使用ContentResolver加载图片失败: $uri", e)
                // 如果ContentResolver加载失败，尝试使用setImageURI
                setImageURI(uri)
            }
            
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

        android.util.Log.d("DiaryDetailActivity", "保存日记 - 标题: $title, 内容长度: ${content.length}, 店铺位置: $selectedShopPosition, 店铺数量: ${shops.size}")

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedShopPosition < 0 || selectedShopPosition >= shops.size) {
            Toast.makeText(this, "请先添加店铺", Toast.LENGTH_SHORT).show()
            android.util.Log.e("DiaryDetailActivity", "店铺选择无效 - 位置: $selectedShopPosition, 店铺数量: ${shops.size}")
            return
        }

        val shopId = shops[selectedShopPosition].id
        val imageUrls = imageUris.joinToString(",") { it.toString() }

        android.util.Log.d("DiaryDetailActivity", "准备保存日记 - shopId: $shopId, 图片数量: ${imageUris.size}")

        val diary = Diary(
            id = if (diaryId != null && diaryId!! > 0) diaryId!! else 0,
            shopId = shopId,
            title = title,
            content = content,
            rating = rating,
            imageUrls = imageUrls,
            tags = tags
        )

        lifecycleScope.launch {
            try {
                // 检查shopId是否存在于Shop表中
                val database = AppDatabase.getDatabase(applicationContext)
                val shop = database.shopDao().getShopById(shopId)
                val finalShopId = if (shop != null) {
                    android.util.Log.d("DiaryDetailActivity", "Shop存在 - shopId: $shopId, shopName: ${shop.name}")
                    shopId
                } else {
                    android.util.Log.w("DiaryDetailActivity", "Shop不存在 - shopId: $shopId，将使用null")
                    null
                }
                
                // 创建最终的diary对象，使用验证后的shopId
                val finalDiary = diary.copy(shopId = finalShopId)
                
                if (diaryId != null && diaryId!! > 0) {
                    viewModel.updateDiary(finalDiary)
                    Toast.makeText(this@DiaryDetailActivity, "更新成功", Toast.LENGTH_SHORT).show()
                    android.util.Log.d("DiaryDetailActivity", "日记更新成功 - ID: $diaryId")
                    // 延迟关闭界面，让用户看到提示
                    delay(1000)
                    finish()
                } else {
                    android.util.Log.d("DiaryDetailActivity", "开始插入新日记")
                    val insertedId = viewModel.addDiaryAndGetId(finalDiary)
                    android.util.Log.d("DiaryDetailActivity", "插入结果 - ID: $insertedId")
                    if (insertedId > 0) {
                        diaryId = insertedId
                        Toast.makeText(this@DiaryDetailActivity, "保存成功", Toast.LENGTH_SHORT).show()
                        android.util.Log.d("DiaryDetailActivity", "日记保存成功 - ID: $insertedId")
                        // 延迟关闭界面，让用户看到提示
                        delay(1000)
                        finish()
                    } else {
                        Toast.makeText(this@DiaryDetailActivity, "保存失败", Toast.LENGTH_SHORT).show()
                        android.util.Log.e("DiaryDetailActivity", "日记保存失败 - 插入ID: $insertedId")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DiaryDetailActivity", "保存日记时发生异常", e)
                Toast.makeText(this@DiaryDetailActivity, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
