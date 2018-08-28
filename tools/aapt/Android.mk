#
# Copyright (C) 2014 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

ifeq ($(TARGET_BUILD_APPS)$(filter true,$(TARGET_BUILD_PDK)),)

# ==========================================================
# Setup some common variables for the different build
# targets here.
# ==========================================================
LOCAL_PATH:= $(call my-dir)

aaptHostStaticLibs := \
    libandroidfw \
    libpng \
    libutils \
    liblog \
    libcutils \
    libexpat \
    libziparchive \
    libbase \
    libz

aaptCFlags := -Wall -Werror

# ==========================================================
# Build the host executable: aapt
# ==========================================================
include $(CLEAR_VARS)

LOCAL_MODULE := aapt
LOCAL_MODULE_HOST_OS := darwin linux windows
LOCAL_CFLAGS := -DAAPT_VERSION=\"$(BUILD_NUMBER_FROM_FILE)\" $(aaptCFlags)
LOCAL_SRC_FILES := Main.cpp
LOCAL_STATIC_LIBRARIES := libaapt $(aaptHostStaticLibs)

include $(BUILD_HOST_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE := aapt
LOCAL_CFLAGS := -DAAPT_VERSION=\"$(BUILD_NUMBER_FROM_FILE)\" $(aaptCFlags)
LOCAL_SRC_FILES := Main.cpp
LOCAL_STATIC_LIBRARIES := libaapt-static $(aaptHostStaticLibs)

LOCAL_MODULE_STEM_32 := aapt
LOCAL_MODULE_STEM_64 := aapt64
LOCAL_MODULE_PATH_32 := $(PRODUCT_OUT)/system/app/bin
LOCAL_MODULE_PATH_64 := $(PRODUCT_OUT)/system/app/bin
LOCAL_MULTILIB := both

include $(BUILD_EXECUTABLE)


endif # No TARGET_BUILD_APPS or TARGET_BUILD_PDK
