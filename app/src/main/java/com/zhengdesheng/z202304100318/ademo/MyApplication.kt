package com.zhengdesheng.z202304100318.ademo

import android.app.Application
import android.util.Log
import com.baidu.mapapi.SDKInitializer

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Log.d("MyApplication", "开始初始化百度地图SDK")
            SDKInitializer.setAgreePrivacy(this, true)
            SDKInitializer.initialize(this)
            Log.d("MyApplication", "百度地图SDK初始化成功")
        } catch (e: Exception) {
            Log.e("MyApplication", "百度地图SDK初始化失败", e)
        }
        Log.d("MyApplication", "应用初始化完成")
    }
}