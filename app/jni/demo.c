//
// Created by Yen Yu Ting on 2022/5/7.
//

#include <string.h>
#include <jni.h>

jstring
Java_org_tensorflow_lite_examples_classification_ClassifierActivity_getStrFromJNI(JNIEnv *env,
                                                          jobject thiz) {
    return  (*env)->NewStringUTF(env, "I`m Str !");
}