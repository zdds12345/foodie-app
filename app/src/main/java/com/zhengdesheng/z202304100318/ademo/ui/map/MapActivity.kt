package com.zhengdesheng.z202304100318.ademo.ui.map

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.InfoWindow
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.map.Text
import com.baidu.mapapi.map.TextOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.poi.PoiCitySearchOption
import com.baidu.mapapi.search.poi.PoiIndoorInfo
import com.baidu.mapapi.search.poi.PoiIndoorResult
import com.baidu.mapapi.search.poi.PoiNearbySearchOption
import com.baidu.mapapi.search.poi.PoiResult
import com.baidu.mapapi.search.poi.PoiSearch
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private var mapView: MapView? = null
    private var baiduMap: BaiduMap? = null
    private var isMapInitialized = false
    private val markers = mutableListOf<com.baidu.mapapi.map.Overlay>()
    private var savedInstanceState: Bundle? = null
    private var poiSearch: PoiSearch? = null
    private val nearbyRestaurants = mutableListOf<Shop>()

    companion object {
        private const val HANGZHOU_LAT = 30.2741
        private const val HANGZHOU_LNG = 120.1551
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            android.util.Log.d("MapActivity", "onCreate 开始")
            
            this.savedInstanceState = savedInstanceState
            super.onCreate(savedInstanceState)
            android.util.Log.d("MapActivity", "super.onCreate 完成")
            
            binding = ActivityMapBinding.inflate(layoutInflater)
            android.util.Log.d("MapActivity", "binding inflate 完成")
            
            setContentView(binding.root)
            android.util.Log.d("MapActivity", "setContentView 完成")
            
            android.util.Log.d("MapActivity", "布局已设置")
            
            setupUI()
            android.util.Log.d("MapActivity", "setupUI 调用完成")
            
            val shopId = intent.getLongExtra("shop_id", -1L)
            val shopName = intent.getStringExtra("shop_name")
            
            android.util.Log.d("MapActivity", "========== MapActivity 启动 ==========")
            android.util.Log.d("MapActivity", "intent extras: shop_id=$shopId, shop_name=$shopName")
            android.util.Log.d("MapActivity", "intent hasExtra('shop_id'): ${intent.hasExtra("shop_id")}")
            android.util.Log.d("MapActivity", "intent hasExtra('shop_name'): ${intent.hasExtra("shop_name")}")
            
            android.util.Log.d("MapActivity", "onCreate 完成")
        } catch (e: Exception) {
            android.util.Log.e("MapActivity", "onCreate 发生异常", e)
            Toast.makeText(this, "页面加载失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupUI() {
        try {
            android.util.Log.d("MapActivity", "setupUI 开始")
            
            if (binding.toolbar == null) {
                android.util.Log.e("MapActivity", "toolbar 为 null")
                return
            }
            android.util.Log.d("MapActivity", "toolbar 不为 null")
            
            setSupportActionBar(binding.toolbar)
            android.util.Log.d("MapActivity", "setSupportActionBar 完成")
            
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            android.util.Log.d("MapActivity", "setDisplayHomeAsUpEnabled 完成")
            
            binding.toolbar.setNavigationOnClickListener { finish() }
            android.util.Log.d("MapActivity", "setNavigationOnClickListener 完成")
            
            if (binding.fabLocation == null) {
                android.util.Log.e("MapActivity", "fabLocation 为 null")
                return
            }
            android.util.Log.d("MapActivity", "fabLocation 不为 null")
            
            binding.fabLocation.setOnClickListener {
                centerMapOnLocation(HANGZHOU_LAT, HANGZHOU_LNG)
                Toast.makeText(this, getString(R.string.centered_on_hangzhou), Toast.LENGTH_SHORT).show()
            }
            android.util.Log.d("MapActivity", "setOnClickListener 完成")
            
            if (binding.fabAddToNearby == null) {
                android.util.Log.e("MapActivity", "fabAddToNearby 为 null")
                return
            }
            android.util.Log.d("MapActivity", "fabAddToNearby 不为 null")
            
            binding.fabAddToNearby.setOnClickListener {
                addNearbyRestaurantsToDatabase()
            }
            
            android.util.Log.d("MapActivity", "setupUI 完成")
        } catch (e: Exception) {
            android.util.Log.e("MapActivity", "setupUI 发生异常", e)
        }
    }

    private fun initializeMap() {
        if (isMapInitialized) {
            android.util.Log.d("MapActivity", "地图已初始化，跳过")
            return
        }
        
        try {
            android.util.Log.d("MapActivity", "开始初始化百度地图")
            
            if (binding.mapContainer == null) {
                android.util.Log.e("MapActivity", "mapContainer 为 null")
                Toast.makeText(this, "地图容器初始化失败", Toast.LENGTH_LONG).show()
                return
            }
            
            android.util.Log.d("MapActivity", "mapContainer 不为 null，准备创建MapView")
            
            try {
                val mapView = MapView(this)
                android.util.Log.d("MapActivity", "MapView 创建成功")
                
                binding.mapContainer.removeAllViews()
                android.util.Log.d("MapActivity", "清空容器完成")
                
                binding.mapContainer.addView(mapView)
                android.util.Log.d("MapActivity", "MapView 已添加到容器")
                
                this.mapView = mapView
                
                mapView.onCreate(this, null)
                android.util.Log.d("MapActivity", "MapView.onCreate 完成")
                
                mapView.onResume()
                android.util.Log.d("MapActivity", "MapView.onResume 完成")
                
                baiduMap = mapView.map
                android.util.Log.d("MapActivity", "获取 BaiduMap 对象: ${baiduMap != null}")
                
                val shopId = intent.getLongExtra("shop_id", -1L)
                val shopName = intent.getStringExtra("shop_name")
                
                if (shopId != -1L) {
                    android.util.Log.d("MapActivity", "准备加载单个店铺，shopId=$shopId")
                    supportActionBar?.title = shopName ?: getString(R.string.food_map)
                    loadAndCenterOnShop(shopId)
                } else {
                    android.util.Log.d("MapActivity", "没有店铺ID，搜索附近餐饮")
                    supportActionBar?.title = getString(R.string.food_map)
                    searchNearbyRestaurants()
                }
                
                isMapInitialized = true
                android.util.Log.d("MapActivity", "地图初始化完成")
            } catch (e: Exception) {
                android.util.Log.e("MapActivity", "创建MapView失败", e)
                Toast.makeText(this, "地图功能暂时不可用: ${e.message}", Toast.LENGTH_LONG).show()
                
                binding.mapContainer.removeAllViews()
                val textView = android.widget.TextView(this).apply {
                    text = "地图功能暂时不可用"
                    textSize = 18f
                    gravity = android.view.Gravity.CENTER
                }
                binding.mapContainer.addView(textView)
                isMapInitialized = true
            }
        } catch (e: Exception) {
            android.util.Log.e("MapActivity", "地图初始化发生异常", e)
            Toast.makeText(this, "地图初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun centerMapOnLocation(lat: Double, lng: Double) {
        val map = baiduMap ?: return
        val point = LatLng(lat, lng)
        val mapStatus = MapStatusUpdateFactory.newLatLngZoom(point, 16f)
        map.setMapStatus(mapStatus)
        android.util.Log.d("MapActivity", "地图中心已设置到: ($lat, $lng)")
    }

    private fun loadAndCenterOnShop(shopId: Long) {
        android.util.Log.d("MapActivity", ">>> loadAndCenterOnShop 开始执行, shopId=$shopId")
        
        // 首先尝试从Intent中获取完整的店铺信息
        val shopName = intent.getStringExtra("shop_name")
        val shopLatitude = intent.getDoubleExtra("shop_latitude", 0.0)
        val shopLongitude = intent.getDoubleExtra("shop_longitude", 0.0)
        val shopAddress = intent.getStringExtra("shop_address")
        
        if (shopName != null && shopLatitude != 0.0 && shopLongitude != 0.0) {
            android.util.Log.d("MapActivity", "✓ 从Intent获取店铺信息: $shopName")
            android.util.Log.d("MapActivity", "  - 经度: $shopLongitude")
            android.util.Log.d("MapActivity", "  - 纬度: $shopLatitude")
            android.util.Log.d("MapActivity", "  - 地址: $shopAddress")
            
            val map = baiduMap
            if (map == null) {
                android.util.Log.e("MapActivity", "baiduMap 为 null，无法加载地图")
                return
            }
            
            clearMarkers()
            
            val point = LatLng(shopLatitude, shopLongitude)
            val bitmap = BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation)
            val option = MarkerOptions()
                .position(point)
                .icon(bitmap)
                .title(shopName)
            
            val marker = (map.addOverlay(option) as? Marker)
            marker?.let { markers.add(it) }
            
            val textOption = TextOptions()
                .text(shopName)
                .position(point)
                .fontSize(24)
                .fontColor(android.graphics.Color.BLACK)
                .bgColor(android.graphics.Color.parseColor("#FFFFFF"))
                .rotate(0f)
                .yOffset(-50)
            
            val text = map.addOverlay(textOption) as? Text
            text?.let { markers.add(it) }
            
            val mapStatus = MapStatusUpdateFactory.newLatLngZoom(point, 16f)
            map.setMapStatus(mapStatus)
            
            map.setOnMarkerClickListener { marker ->
                showInfoWindow(marker, shopName, shopAddress ?: "")
                true
            }
            
            Toast.makeText(this@MapActivity, "已定位到 $shopName", Toast.LENGTH_SHORT).show()
            android.util.Log.d("MapActivity", "✓ 店铺地图加载完成")
        } else {
            // 如果Intent中没有完整信息，回退到数据库查询
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val shopDao = AppDatabase.getDatabase(applicationContext).shopDao()
                    android.util.Log.d("MapActivity", "正在查询数据库, shopId=$shopId")
                    val shop = shopDao.getShopById(shopId)
                    
                    withContext(Dispatchers.Main) {
                        if (shop != null) {
                            android.util.Log.d("MapActivity", "✓ 从数据库找到店铺: ${shop.name}")
                            android.util.Log.d("MapActivity", "  - 经度: ${shop.longitude}")
                            android.util.Log.d("MapActivity", "  - 纬度: ${shop.latitude}")
                            android.util.Log.d("MapActivity", "  - 地址: ${shop.address}")
                            
                            val map = baiduMap
                            if (map == null) {
                                android.util.Log.e("MapActivity", "baiduMap 为 null，无法加载地图")
                                return@withContext
                            }
                            
                            clearMarkers()
                            
                            val point = LatLng(shop.latitude, shop.longitude)
                            val bitmap = BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation)
                            val option = MarkerOptions()
                                .position(point)
                                .icon(bitmap)
                                .title(shop.name)
                            
                            val marker = (map.addOverlay(option) as? Marker)
                            marker?.let { markers.add(it) }
                            
                            val textOption = TextOptions()
                                .text(shop.name)
                                .position(point)
                                .fontSize(24)
                                .fontColor(android.graphics.Color.BLACK)
                                .bgColor(android.graphics.Color.parseColor("#FFFFFF"))
                                .rotate(0f)
                                .yOffset(-50)
                            
                            val text = map.addOverlay(textOption) as? Text
                            text?.let { markers.add(it) }
                            
                            val mapStatus = MapStatusUpdateFactory.newLatLngZoom(point, 16f)
                            map.setMapStatus(mapStatus)
                            
                            map.setOnMarkerClickListener { marker ->
                                showInfoWindow(marker, shop.name, shop.address)
                                true
                            }
                            
                            Toast.makeText(this@MapActivity, "已定位到 ${shop.name}", Toast.LENGTH_SHORT).show()
                            android.util.Log.d("MapActivity", "✓ 店铺地图加载完成")
                        } else {
                            android.util.Log.d("MapActivity", "✗ 未找到店铺: shopId=$shopId")
                            Toast.makeText(this@MapActivity, getString(R.string.shop_not_found), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MapActivity", "✗ 加载单个店铺失败", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapActivity, "加载商家失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun searchNearbyRestaurants() {
        android.util.Log.d("MapActivity", "开始搜索附近餐饮")
        
        poiSearch = PoiSearch.newInstance()
        val listener = PoiSearchListener { result ->
            android.util.Log.d("MapActivity", "POI搜索完成")
            
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                val allPois = result.allPoi
                android.util.Log.d("MapActivity", "搜索到 ${allPois.size} 个餐饮")
                
                nearbyRestaurants.clear()
                
                val map = baiduMap
                if (map == null) {
                    android.util.Log.e("MapActivity", "baiduMap 为 null，无法显示搜索结果")
                    return@PoiSearchListener
                }
                
                clearMarkers()
                
                val bitmap = BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation)
                
                allPois.take(20).forEach { poi ->
                    val point = poi.location
                    val shop = Shop(
                        name = poi.name,
                        category = "餐饮",
                        address = poi.address,
                        latitude = point.latitude,
                        longitude = point.longitude,
                        rating = 4.0f,
                        imageUrl = null,
                        phone = poi.phoneNum ?: "",
                        businessHours = ""
                    )
                    nearbyRestaurants.add(shop)
                    
                    val option = MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .title(poi.name)
                    
                    val marker = (map.addOverlay(option) as? Marker)
                    marker?.let { markers.add(it) }
                    
                    val textOption = TextOptions()
                        .text(poi.name)
                        .position(point)
                        .fontSize(24)
                        .fontColor(android.graphics.Color.BLACK)
                        .bgColor(android.graphics.Color.parseColor("#FFFFFF"))
                        .rotate(0f)
                        .yOffset(-50)
                    
                    val text = map.addOverlay(textOption) as? Text
                    text?.let { markers.add(it) }
                }
                
                val mapStatus = MapStatusUpdateFactory.newLatLngZoom(LatLng(HANGZHOU_LAT, HANGZHOU_LNG), 15f)
                map.setMapStatus(mapStatus)
                
                map.setOnMarkerClickListener { marker ->
                    val title = marker.title
                    val shop = nearbyRestaurants.find { it.name == title }
                    if (shop != null) {
                        showInfoWindow(marker, shop.name, shop.address)
                    }
                    true
                }
                
                Toast.makeText(this@MapActivity, "搜索到 ${nearbyRestaurants.size} 家附近餐饮", Toast.LENGTH_SHORT).show()
                android.util.Log.d("MapActivity", "附近餐饮搜索完成")
                
                // 自动将搜索结果保存到数据库
                addNearbyRestaurantsToDatabase()
            } else {
                android.util.Log.e("MapActivity", "POI搜索失败，errorCode=${result.error}")
                Toast.makeText(this@MapActivity, "搜索附近餐饮失败", Toast.LENGTH_SHORT).show()
            }
        }
        poiSearch?.setOnGetPoiSearchResultListener(listener)
        
        val nearbySearchOption = PoiNearbySearchOption()
            .keyword("餐饮")
            .location(LatLng(HANGZHOU_LAT, HANGZHOU_LNG))
            .radius(2000)
            .pageNum(0)
            .pageCapacity(20)
        
        poiSearch?.searchNearby(nearbySearchOption)
        android.util.Log.d("MapActivity", "发起POI搜索请求")
    }

    private fun loadAllShops() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val shopDao = AppDatabase.getDatabase(applicationContext).shopDao()
                val shops = shopDao.getAllShops().first()
                
                android.util.Log.d("MapActivity", "从数据库加载到 ${shops.size} 个店铺")
                
                withContext(Dispatchers.Main) {
                    if (shops.isEmpty()) {
                        Toast.makeText(this@MapActivity, "数据库中没有店铺数据，请先添加店铺", Toast.LENGTH_LONG).show()
                        android.util.Log.d("MapActivity", "店铺列表为空")
                    } else {
                        Toast.makeText(this@MapActivity, "已加载 ${shops.size} 个店铺", Toast.LENGTH_SHORT).show()
                        android.util.Log.d("MapActivity", "开始加载地图")
                        
                        val map = baiduMap
                        if (map == null) {
                            android.util.Log.e("MapActivity", "baiduMap 为 null，无法加载地图")
                            return@withContext
                        }
                        
                        clearMarkers()
                        
                        val bitmap = BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation)
                        
                        shops.forEach { shop ->
                            val point = LatLng(shop.latitude, shop.longitude)
                            val option = MarkerOptions()
                                .position(point)
                                .icon(bitmap)
                                .title(shop.name)
                            
                            val marker = (map.addOverlay(option) as? Marker)
                            marker?.let { markers.add(it) }
                            
                            val textOption = TextOptions()
                                .text(shop.name)
                                .position(point)
                                .fontSize(24)
                                .fontColor(android.graphics.Color.BLACK)
                                .bgColor(android.graphics.Color.parseColor("#FFFFFF"))
                                .rotate(0f)
                                .yOffset(-50)
                            
                            val text = map.addOverlay(textOption) as? Text
                            text?.let { markers.add(it) }
                        }
                        
                        val mapStatus = MapStatusUpdateFactory.newLatLngZoom(LatLng(HANGZHOU_LAT, HANGZHOU_LNG), 13f)
                        map.setMapStatus(mapStatus)
                        
                        map.setOnMarkerClickListener { marker ->
                            val title = marker.title
                            val shop = shops.find { it.name == title }
                            if (shop != null) {
                                showInfoWindow(marker, shop.name, shop.address)
                            }
                            true
                        }
                        
                        android.util.Log.d("MapActivity", "所有店铺地图加载完成")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MapActivity", "加载店铺失败", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "加载店铺失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun addNearbyRestaurantsToDatabase() {
        if (nearbyRestaurants.isEmpty()) {
            Toast.makeText(this, "没有可添加的餐饮", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val shopDao = AppDatabase.getDatabase(applicationContext).shopDao()
                
                // 清除原有数据，实现同步功能
                shopDao.deleteAllShops()
                android.util.Log.d("MapActivity", "已清除原有店铺数据")
                
                // 添加所有搜索到的餐饮
                shopDao.insertShops(nearbyRestaurants)
                android.util.Log.d("MapActivity", "已添加 ${nearbyRestaurants.size} 家餐饮到数据库")
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "已将 ${nearbyRestaurants.size} 家餐饮同步到附近美食", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("MapActivity", "同步餐饮失败", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "同步失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearMarkers() {
        markers.forEach { marker ->
            marker.remove()
        }
        markers.clear()
        android.util.Log.d("MapActivity", "已清除所有标记")
    }

    private fun showInfoWindow(marker: Marker, title: String, address: String) {
        val map = baiduMap ?: return
        val point = marker.position
        
        val textView = android.widget.TextView(this).apply {
            text = "$title\n$address"
            textSize = 14f
            setPadding(20, 10, 20, 10)
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        
        val infoWindow = InfoWindow(textView, point, -47)
        map.showInfoWindow(infoWindow)
        android.util.Log.d("MapActivity", "显示店铺信息: $title - $address")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("MapActivity", "onResume 开始")
        initializeMap()
        mapView?.onResume()
        android.util.Log.d("MapActivity", "onResume 完成")
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d("MapActivity", "onPause 开始")
        mapView?.onPause()
        android.util.Log.d("MapActivity", "onPause 完成")
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("MapActivity", "onDestroy 开始")
        clearMarkers()
        mapView?.onDestroy()
        mapView = null
        baiduMap = null
        android.util.Log.d("MapActivity", "onDestroy 完成")
    }
}
