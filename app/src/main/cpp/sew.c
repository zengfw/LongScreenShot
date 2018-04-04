#include <jni.h>
#include <android/bitmap.h>

JNIEXPORT jstring JNICALL Java_com_zfw_screenshot_utils_SewUtils_stringFromJNI(JNIEnv *env, jclass  jcls) {
    jstring result = (*env)->NewStringUTF(env, "I am String type.");
    return result;
}

/*
JNIEXPORT void JNICALL Java_zfw_com_cmakedemo_MainActivity_transferBitmap(JNIEnv *env, jclass jcls, jobject bmp1) {

    // 1.Get bitmap info
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bmp1, &info);
    // 2.Check format, only RGB565 & RGBA are supported
    if (info.width <= 0 || info.height <= 0 ||
        (info.format != ANDROID_BITMAP_FORMAT_RGB_565 && info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)) {
        return;
    }
    // 3.Lock the bitmap to get the buffer
    void * pixels = NULL;
    AndroidBitmap_lockPixels(env, bmp1, &pixels);
    if (pixels == NULL) {
        return;
    }

    // 4.unlock
    AndroidBitmap_unlockPixels(env, bmp1);
}
*/