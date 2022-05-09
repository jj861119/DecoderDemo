LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE:= bch
LOCAL_SRC_FILES:=  bch.c bchlib.c
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include/
LOCAL_LDLIBS +=-llog
include $(BUILD_SHARED_LIBRARY)
