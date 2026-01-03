package com.zhengdesheng.z202304100318.ademo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DISPLAY_TIME = 2000L // 2秒

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 设置学号和姓名
        val textViewStudentInfo = findViewById<TextView>(R.id.textViewStudentInfo)
        textViewStudentInfo.text = "学号：202304100318\n姓名：郑德胜"

        // 延迟后跳转到主页面
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_DISPLAY_TIME)
    }
}