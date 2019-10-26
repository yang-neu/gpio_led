#include <jni.h>
#include <string>
#include <stdlib.h>


extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_gpio_1led_1jni_MainActivity_stringFromJNI(
Java_com_example_gpio_1led_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from JNI";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_gpio_1led_MainActivity_helloFromJNI(
        JNIEnv *env,
jobject /* this */) {
std::string hello = "Hello JNI, You are great!";
return env->NewStringUTF(hello.c_str());
}


