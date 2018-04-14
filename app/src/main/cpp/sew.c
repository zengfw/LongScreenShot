#include <jni.h>
#include <android/bitmap.h>

JNIEXPORT jint JNICALL Java_com_zfw_screenshot_utils_SewUtils_compare(
        JNIEnv *env, jobject thiz, jobject bmp1, jobject bmp2, int h1, int h2, int len) {

//    int[] pixel_1 = new int[len];
//    int[] pixel_2 = new int[len];

    int y1 = h1 - 1;
    int y2 = h2 - 1;

    int result = 5;
    return result;
}

JNIEXPORT int JNICALL Java_com_zfw_screenshot_utils_SewUtils_isSameRow(
        JNIEnv *env, jobject thiz, jintArray arr1, jintArray arr2, int length) {

    jint *pixel_1 = (*env)->GetIntArrayElements(env, arr1, NULL);
    jint *pixel_2 = (*env)->GetIntArrayElements(env, arr2, NULL);

    // 像素的对比可能不能这么对比
    for (int i = 0; i < length; i++) {
        if (pixel_1[i] != pixel_2[i]) {
            return 0;
        }
    }
    return 1;

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