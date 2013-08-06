LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := chibi
LOCAL_CFLAGS := $(ANDROID_CFLAGS)
LOCAL_LDFLAGS := -L$(LOCAL_PATH)/../.. $(ANDROID_LDFLAGS)


CHIBI_FILES := \
	gc.c \
	sexp.c \
	bignum.c \
	opcodes.c \
	vm.c \
	eval.c \
	simlify.c
# $(CHIBI_FILES:%=$(LOCAL_PATH)/../../%)

LOCAL_SRC_FILES := chibi.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../include

# LOCAL_STATIC_LIBRARIES := mupdfcore mupdfthirdparty djvudroid

# uses Android log and z library (Android-3 Native API)
LOCAL_LDLIBS := -lm -lz -llog -ljnigraphics -lchibi-scheme


include $(BUILD_SHARED_LIBRARY)

