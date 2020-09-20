LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := SetupWizard
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_OVERRIDES_PACKAGES := Provision

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.core_core \
    androidx.recyclerview_recyclerview \
    setupcompat \
    setupdesign

LOCAL_STATIC_JAVA_LIBRARIES := \
    libphonenumber

LOCAL_JAVA_LIBRARIES := \
    telephony-common

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_USE_AAPT2 := true

LOCAL_REQUIRED_MODULES := privapp_whitelist_org.calyxos.setupwizard.xml

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE := privapp_whitelist_org.calyxos.setupwizard.xml
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
