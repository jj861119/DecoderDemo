/*
 * Python C module for BCH encoding/decoding.
 *
 * Copyright © 2013, 2017-2018 Jeff Kent <jeff@jkent.net>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

#include <errno.h>
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include "include/bch.h"
#include <Android/Log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,"bchlib" ,__VA_ARGS__)

typedef struct {
	struct bch_control *bch;
	int reversed;
} BCHObject;

static int BCH_init(BCHObject *self);
static int BCH_decode_inplace(uint8_t *data,uint8_t *ecc);
static jstring unsigchar2jstring(JNIEnv *e, unsigned char* pChar);

jstring
Java_org_tensorflow_lite_examples_classification_ClassifierActivity_BCHDecode(JNIEnv *env,
																				  jobject this,jbyteArray data,jbyteArray ecc) {
	jbyte* bytedata = (*env)->GetByteArrayElements(env,data, 0);
	//jsize  oldsize = (*env)->GetArrayLength(env,data);
	uint8_t* data_char = (uint8_t*)bytedata;

    jbyte* byteecc= (*env)->GetByteArrayElements(env,ecc, 0);
	//jsize  oldsize = (*env)->GetArrayLength(env,data);
	uint8_t* ecc_char = (uint8_t*)byteecc;



    int bitflips = BCH_decode_inplace(data_char, ecc_char);
    //char* result = (char *)malloc( 8 * sizeof(char));
	unsigned char result[8]="";
	result[7]='\0';

    
	if(bitflips!=-1)
	{
		for(int i =0;i<7;i++)
		{
			result[i]=(unsigned char)data_char[i];
            //LOGD("%d\n", (int)data_char[i]);
		}
	}

	//jbyteArray newByteArray = (*env)->NewByteArray(env,8);
    //把jint指標中的元素設定到jintArray物件中
    //(*env)->SetByteArrayRegion(env,newByteArray, 0, 8, data_char);
	//printf("%d\n", bitflips);
	jstring result_s;
	result_s = unsigchar2jstring(env, result);
    LOGD("Result %s\n", result);

	//const char* errorKind = NULL;
    //uint8_t utf8 = checkUtfBytes2(result, &errorKind);
    //if (errorKind != NULL && utf8 ==0) {
    //    result_s = (*env)->NewStringUTF(env, result);
    //}
    //else
    //{
    //    result_s = (*env)->NewStringUTF(env, "");
    //}

	//try:
	    //result_s = (*env)->NewStringUTF(env, result);
	//except:
    	//result_s = (*env)->NewStringUTF(env, "");

	return result_s;
	//return  bitflips;
}

static jstring unsigchar2jstring(JNIEnv *e, unsigned char* pChar){
    unsigned char *newresult = pChar;
    //定义java String类 clsstring
    jclass clsstring = (*e)->FindClass(e,"java/lang/String");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID mid = (*e)->GetMethodID(e,clsstring , "<init>" , "([BLjava/lang/String;)V");
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (*e)->NewStringUTF(e,"utf-8");
    //建立byte数组
    jbyteArray bytes = (*e)->NewByteArray(e,strlen((char*)newresult));
    //将char* 转换为byte数组
    (*e)->SetByteArrayRegion(e,bytes, 0, strlen((char*)newresult), (jbyte*) newresult);
    //将byte数组转换为java String,并输出
    return (jstring) (*e)->NewObject(e,clsstring, mid, bytes, encoding);
}

static int
BCH_init(BCHObject *self)
{
	unsigned int prim_poly=137;
	int t=5;
	//PyObject *reversed = NULL;
	//static char *kwlist[] = {"polynomial", "t", "reverse", NULL};
	int m;
	unsigned int tmp;

	//if (!PyArg_ParseTupleAndKeywords(args, kwds, "Ii|O", kwlist,
	//		&prim_poly, &t, &reversed)) {
	//	return -1;
	//}

	//self->reversed = 0;
	//if (reversed) {
	//	Py_INCREF(reversed);
	//	self->reversed = (PyObject_IsTrue(reversed) == 1);
	//	Py_DECREF(reversed);
	//}

	tmp = prim_poly;
	m = 0;
	while (tmp >>= 1) {
		m++;
	}

	self->bch = init_bch(m, t, prim_poly);
	if (!self->bch) {
		return -1;
	}

	return 0;
}

static int
BCH_decode_inplace(uint8_t *data,uint8_t *ecc)
{
    //BCH_init(self);
	
	BCHObject self;
	unsigned int prim_poly=137;
	int t=5;
	int m;
	unsigned int tmp;
	tmp = prim_poly;
	m = 0;
	while (tmp >>= 1) {
		m++;
	}

	self.bch = init_bch(m, t, prim_poly);
	if (!self.bch) {
		return -1;
	}




	unsigned int *errloc = alloca(sizeof(unsigned int) * 5);
	int result = -1;
    int data_len=7;
    int ecc_len=5;


	if (ecc_len != self.bch->ecc_bytes) {
		// PyErr_Format(PyExc_ValueError,
		// 	"ecc length should be %d bytes",
		// 	self->bch->ecc_bytes);
		return -1;
	}

	// if (self->reversed) {
	// 	reverse_bytes(data.buf, data.buf, data.len);
	// }

	int nerr = decode_bch(self.bch, data, (unsigned int)7, ecc,
				NULL, NULL, errloc);
    // int decode_bch(struct bch_control *bch, const uint8_t *data, unsigned int len,
	//        const uint8_t *recv_ecc, const uint8_t *calc_ecc,
	//        const unsigned int *syn, unsigned int *errloc)

	if (nerr < 0) {
		if (nerr == -EINVAL) {
			// PyErr_SetString(PyExc_ValueError,
			// 	"invalid parameters");
			goto cleanup;
		} else if (nerr == -EBADMSG) {
			nerr = -1;
		} else {
			goto cleanup;
		}
	}

	for (int i = 0; i < nerr; i++) {
		unsigned int bitnum = errloc[i];
		if (bitnum >= data_len*8 + ecc_len*8) {
			// PyErr_SetString(PyExc_IndexError,
			// 	"uncorrectable error");
			goto cleanup;
		}
		if (bitnum < data_len*8) {
			((char *)data)[bitnum/8] ^= 1 << (bitnum & 7);
		} else {
			((char *)ecc)[bitnum/8 - data_len] ^= 1 << (bitnum & 7);
		}
	}

	// if (self->reversed) {
	// 	reverse_bytes(data.buf, data.buf, data.len);
	// }

	//result = PyLong_FromLong(nerr);
	result = nerr;

cleanup:
    free(data);
    free(ecc);
	//return 123;
	return result;
}