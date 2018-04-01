#include <jni.h>

JNIEXPORT jstring JNICALL Java_com_zfw_screenshot_utils_SewUtils_stringFromJNI(JNIEnv *env, jclass  jcls) {
    jstring result = (*env)->NewStringUTF(env, "I am String type.");
    return result;
}
