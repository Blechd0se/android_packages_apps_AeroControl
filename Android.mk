LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_SRC_FILES += $(call all-java-files-under, src)
 
LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4
 
LOCAL_PACKAGE_NAME := ParanoidOTA
LOCAL_CERTIFICATE := platform
 
include $(BUILD_PACKAGE)

# Support library v4
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    android-support-v4:/libs/android-support-v4.jar

include $(BUILD_MULTI_PREBUILT)
