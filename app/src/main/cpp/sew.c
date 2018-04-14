#include <jni.h>
#include <android/bitmap.h>
#include <stdlib.h>

void getRowPixels(int *target, int y0, int *src, int y1, int len) {
    int target_start_pos = y0 * len;
    int src_start_pos = y1 * len;
    int index = 0;
    for(; index < len; index++) {
        target[target_start_pos + index] = src[src_start_pos + index];
    }
}

int comparePixel(int *pixel_1, int *pixel_2, int length) {
    int i;
    for (i = 0; i < length; i++) {
        if (pixel_1[i] != pixel_2[i]) {
            return 0;
        }
    }
    return 1;
}

int* lockPixel(JNIEnv *env, jobject bmp) {
    int *pixels_1 = NULL;
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, bmp, &info) < 0) {
        return NULL;
    }
    AndroidBitmap_lockPixels(env, bmp, (void**)&pixels_1);
    if (pixels_1 == NULL) {
        return NULL;
    }
    return pixels_1;
}

void unlockPixel(JNIEnv *env, jobject bmp) {
    AndroidBitmap_unlockPixels(env, bmp);
}

JNIEXPORT void JNICALL Java_com_zfw_screenshot_utils_SewUtils_merge(
        JNIEnv *env, jobject thiz, jobject bmp0, jobject bmp1, jobject bmp2, int h0, int h1, int h2, int samePart, int len) {

    int *pixels_0 = lockPixel(env, bmp0);
    int *pixels_1 = lockPixel(env, bmp1);
    int *pixels_2 = lockPixel(env, bmp2);
    /* -------------------- merge the difference ----------------------- */
    int index = 0;
    while(index < h0) {
        if(index < h1) {
            getRowPixels(pixels_0, index, pixels_1, index, len);
        } else {
            getRowPixels(pixels_0, index, pixels_2, index - h1 + samePart, len);
        }
        index++;
    }
    /* -------------------- merge the difference ----------------------- */
    unlockPixel(env, bmp0);
    unlockPixel(env, bmp1);
    unlockPixel(env, bmp2);
}

JNIEXPORT int JNICALL Java_com_zfw_screenshot_utils_SewUtils_compare(
        JNIEnv *env, jobject thiz, jobject bmp1, jobject bmp2, int h1, int h2, int len) {

    int *pixels_1 = lockPixel(env, bmp1);
    int *pixels_2 = lockPixel(env, bmp2);
    if(pixels_1 == NULL || pixels_2 == NULL) {
        return 0;
    }

    int *row_pixel_1 = malloc(sizeof(int) * len);
    int *row_pixel_2 = malloc(sizeof(int) * len);
    int y1 = h1 - 1;
    int y2 = h2 - 1;

    getRowPixels(row_pixel_1, 0, pixels_1, y1, len);
    while(1) {
        getRowPixels(row_pixel_2, 0, pixels_2, y2, len);
        if(comparePixel(row_pixel_1, row_pixel_2, len)) {
            int currY1 = y1 - 1;
            int currY2 = y2 - 1;
            int count = 40;
            int flag = 0;
            int i;
            for(i = 0; i < count; i++) {
                if (currY2 < 0) {
                    break;
                }
                getRowPixels(row_pixel_1, 0, pixels_1, currY1, len);
                getRowPixels(row_pixel_2, 0, pixels_2, currY2, len);
                if (comparePixel(row_pixel_1, row_pixel_2, len)) {
                    flag++;
                    currY1--;
                    currY2--;
                    if (flag == count) {
                        return y2;
                    }
                } else {
                    getRowPixels(row_pixel_1, 0, pixels_1, y1, len);
                    break;
                }
            }
        }
        y2--;
        if (y1 < 0 || y2 < 0) {
            break;
        }
    }
    unlockPixel(env, bmp1);
    unlockPixel(env, bmp2);
    free(row_pixel_1);
    free(row_pixel_2);
    return 0;
}