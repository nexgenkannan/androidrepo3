LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= rcscommon
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_SRC_FILES := $(call all-java-files-under,src) \
                   src/org/codeaurora/rcscommon/EnrichedCallUpdateCallback.aidl \
                   src/org/codeaurora/rcscommon/IncomingEnrichedCallCallback.aidl \
                   src/org/codeaurora/rcscommon/NewCallComposerCallback.aidl \
                   src/org/codeaurora/rcscommon/INewPostCallCallback.aidl \
                   src/org/codeaurora/rcscommon/PostCallCapabilitiesCallback.aidl \
                   src/org/codeaurora/rcscommon/RichCallCapabilitiesCallback.aidl \
                   src/org/codeaurora/rcscommon/SessionStateUpdateCallback.aidl \
                   src/org/codeaurora/rcscommon/FetchImageCallBack.aidl \
                   src/org/codeaurora/rcscommon/IRCSService.aidl \

include $(BUILD_JAVA_LIBRARY)

# ============================================================
# Install the permissions file into system/etc/permissions
# ============================================================
include $(CLEAR_VARS)

LOCAL_MODULE := rcscommon.xml
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)

