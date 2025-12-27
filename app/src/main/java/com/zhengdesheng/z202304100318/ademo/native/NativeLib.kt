package com.zhengdesheng.z202304100318.ademo.native

object NativeLib {

    init {
        System.loadLibrary("ademo")
    }

    external fun stringFromJNI(): String

    external fun playSpinSound()

    external fun playWinSound()

    external fun playClickSound()
}
