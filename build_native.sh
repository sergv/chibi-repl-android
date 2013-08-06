#!/bin/bash
#
# File: build.sh
#
# Created: Friday,  2 August 2013
#

set -e

# Notes:
# 1. (add-module-directory) for searching *.sld modules
# 2. import (scheme base) on start for things like equal? for circular structures

# pushd ..
# make CFLAGS=-g0\ -O3 CPPFLAGS=-DSEXP_MAXIMUM_HEAP_SIZE=$((8 * 1024 * 1024))
# popd

if [ ! -f "jni/libchibi-scheme.so" ]; then
    echo "library jni/libchibi-scheme.so not found, but is neened to proceed" >&2
    exit 1
fi

for build_type in "BUILD_ARMV7=0"; do # "BUILD_ARMV7=1"; do
    ${NDK_HOME}/ndk-build APP_BUILD_SCRIPT=./jni/Application.mk $build_type NDEBUG=1 "${@}"
done

cp "jni/libchibi-scheme.so" libs/armeabi/
# ~/projects/android/android-ndk-standalone/bin/arm-linux-androideabi-strip libs/armeabi/*.so

exit 0

