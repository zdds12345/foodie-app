package com.zhengdesheng.z202304100318.ademo

import android.app.Application
import com.baidu.mapapi.SDKInitializer

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(this)
    }
}