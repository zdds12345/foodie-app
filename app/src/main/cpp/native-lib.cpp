#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "NativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_zhengdesheng_z202304100318_ademo_native_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++!";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_zhengdesheng_z202304100318_ademo_native_NativeLib_playSpinSound(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Play spin sound");
}

extern "C" JNIEXPORT void JNICALL
Java_com_zhengdesheng_z202304100318_ademo_native_NativeLib_playWinSound(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Play win sound");
}

extern "C" JNIEXPORT void JNICALL
Java_com_zhengdesheng_z202304100318_ademo_native_NativeLib_playClickSound(
        JNIEnv* env,
        jobject /* this */) {
    LOGD("Play click sound");
}
