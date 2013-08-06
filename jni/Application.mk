LOCAL_PATH := $(call my-dir)

# include $(CLEAR_VARS)

ifndef BUILD_ARMV7
    BUILD_ARMV7 := 0
endif
ifndef NDEBUG
    NDEBUG := 0
endif

$(info "BUILD_ARMV7 = $(BUILD_ARMV7)")
$(info "NDEBUG = $(NDEBUG)")

ifeq ($(BUILD_ARMV7), 0)
TARGET_ARCH_ABI := armeabi
APP_ABI := armeabi
else
TARGET_ARCH_ABI := armeabi-v7a
APP_ABI := armeabi-v7a
endif


NDK_TOOLCHAIN_VERSION := 4.8
APP_OPTIM := release
# APP_OPTIM := debug
APP_PLATFORM := android-9
TARGET_CFLAGS :=


ANDROID_CFLAGS := \
    -marm \
    -mthumb \
    -mthumb-interwork \
    -fpic \
    -fomit-frame-pointer \
    -ffunction-sections \
    -funwind-tables \
    -fstack-protector \
    -no-canonical-prefixes \
    -DANDROID=1 \
    -O3 \
    -g0 \
    -fno-unsafe-math-optimizations \
    -fno-tree-vectorize

# -mno-unaligned-doubles \

#-mfloat-abi=hard \

# -mfpu=neon \
# -mfloat-abi=softfp \
# -mfpu=vfpv3-d16

ifeq ($(BUILD_ARMV7), 0)
ANDROID_CFLAGS += \
    -march=armv6 \
    -mfloat-abi=softfp
else
ANDROID_CFLAGS += \
    -march=armv7-a \
    -mcpu=cortex-a9 \
    -mfloat-abi=softfp \
    -mfpu=vfpv3
endif

ifeq ($(NDEBUG), 0)
    ANDROID_CFLAGS += -UNDEBUG
else
    ANDROID_CFLAGS += -DNDEBUG=1
endif

include $(LOCAL_PATH)/Android.mk

