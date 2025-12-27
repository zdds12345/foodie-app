package com.zhengdesheng.z202304100318.ademo.ui.map

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zhengdesheng.z202304100318.ademo.R
import com.zhengdesheng.z202304100318.ademo.data.database.AppDatabase
import com.zhengdesheng.z202304100318.ademo.data.entity.Shop
import com.zhengdesheng.z202304100318.ademo.databinding.ActivityMapBinding
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    companion object {
        private const val HANGZHOU_LAT = 30.2741
        private const val HANGZHOU_LNG = 120.1551
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWebView()
        setupUI()
        
        val shopId = intent.getLongExtra("shop_id", -1L)
        val shopName = intent.getStringExtra("shop_name")
        
        if (shopId != -1L) {
            supportActionBar?.title = shopName ?: getString(R.string.food_map)
            loadAndCenterOnShop(shopId)
        } else {
            loadMap(HANGZHOU_LAT, HANGZHOU_LNG, 12)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                allowFileAccess = true
                allowContentAccess = true
                mediaPlaybackRequiresUserGesture = false
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("MapActivity", "Page loaded: $url")
                }
                
                override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    Log.e("MapActivity", "WebView error: ${error?.description}")
                }
            }
            webChromeClient = WebChromeClient()
        }
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.fabLocation.setOnClickListener {
            loadMap(HANGZHOU_LAT, HANGZHOU_LNG, 14)
            Toast.makeText(this, "定位到杭州市中心", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMap(lat: Double, lng: Double, zoom: Int) {
        val mapHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body, #map { width: 100%; height: 100%; overflow: hidden; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <script>
                    console.log('Loading map...');
                    try {
                        var map = L.map('map', {
                            center: [$lat, $lng],
                            zoom: $zoom,
                            zoomControl: true
                        });
                        
                        console.log('Map created');
                        
                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '© OpenStreetMap'
                        }).addTo(map);
                        
                        console.log('Tile layer added');
                        
                        L.marker([$lat, $lng]).addTo(map)
                            .bindPopup('Location')
                            .openPopup();
                            
                        console.log('Marker added');
                    } catch(e) {
                        console.error('Error loading map:', e);
                        document.body.innerHTML = '<div style="padding: 20px; color: red;">Error: ' + e.message + '</div>';
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
        
        Log.d("MapActivity", "Loading map at lat=$lat, lng=$lng, zoom=$zoom")
        binding.webView.loadDataWithBaseURL("file:///android_asset/", mapHtml, "text/html", "UTF-8", null)
    }

    private fun loadAndCenterOnShop(shopId: Long) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                val shopDao = database.shopDao()
                val shop = shopDao.getShopById(shopId)
                shop?.let {
                    loadMap(it.latitude, it.longitude, 16)
                    Log.d("MapActivity", "Loaded map for shop: ${it.name}")
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Failed to load shop", e)
                loadMap(HANGZHOU_LAT, HANGZHOU_LNG, 12)
            }
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.destroy()
    }
}
