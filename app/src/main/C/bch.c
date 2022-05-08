//
// Created by Yen Yu Ting on 2022/5/7.
//


#include <string.h>
#include <jni.h>
jstring Java_io_arik_medium_1example_NativeLibrary_greeting(JNIEnv* env, jobject self)
{
    return (*env)->NewStringUTF(env, "Hello from C!");
}