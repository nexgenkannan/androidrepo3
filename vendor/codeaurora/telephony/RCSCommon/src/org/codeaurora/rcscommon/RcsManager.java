/* Copyright (c) 2016, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.codeaurora.rcscommon;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RcsManager {

    private static final String TAG = "RcsManager";
    public static final boolean DBG = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final String RCS_APP_ACTION = "com.qualcomm.qti.action.RCS_SERVICE_AVAILABLE";
    private static final String CONFIG_ENRICHED_CALL_CAPABLE = "config_enriched_call_capable";

    public static final String ENRICH_CALL_CONTENT = "enrich_call_content";
    public static final String ENRICH_CALL_INTENT_EXTRA = "enrich_call_intent_extra";
    public static final String GENERIC_MESSAGE
            = "Service is not connected or function parameters are null";

    private static final String SYSPROP_HW = "ro.boot.hardware";
    private static final String SYSPROP_PRODUCT_BRAND = "ro.product.brand";
    private static final String SYSPROP_RCS_ENABLE = "persist.radio.enriched_call";
    private static final String HW_NAME = "qcom";
    private static final String TARGET_PRODUCT_NAME ="LYF";

    public static final String RCSSERVICE_PUB_NAME = "enrichrcsservice";

    public static final String RCS_APP_START_ACTION = "com.qti.action.RCS_SERVICE_START";

    private static RcsManager sInstance = null;

    private Context mContext = null;
    private IRCSService mRcsService = null;
    private boolean mIsQcomHardware, mIsLYFProduct, mIsRcsEnabled;
    private RcsServiceDeathRecipient mDeathRecipient = new RcsServiceDeathRecipient();

    public synchronized static RcsManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RcsManager(context);
        }
        return sInstance;
    }

    /* single instance class */
    private RcsManager(Context context) {
        log("RcsManager instance");
        mContext = context;

        mIsQcomHardware = HW_NAME.equals(
                    SystemProperties.get(SYSPROP_HW, ""));

        mIsLYFProduct = TARGET_PRODUCT_NAME.equals(
                    SystemProperties.get(SYSPROP_PRODUCT_BRAND,""));

        mIsRcsEnabled = SystemProperties.getBoolean(
                    SYSPROP_RCS_ENABLE, false);

    }

    /**
     * This function will be called to start the service and bind to it. if
     * already connected then notify service connected to
     * ServiceConnectionStatusListener.
     */
    public void initialize() {
        log("initialize");
        if(getService() != null) {
            log("Service is already connected.");
            return;
        }
        ComponentName compName = getRcsServiceComponent();
        if (compName == null)  {
            log("error component did not found to start service");
            return;
        }
        log("starting service with name : " + compName);
        Intent intent = new Intent();
        intent.setClassName(compName.getPackageName(), compName.getClassName());
        mContext.startService(intent);
    }

    private IRCSService getService() {
        if (mRcsService != null) {
            return mRcsService;
        }

        IBinder binder = ServiceManager.getService(RCSSERVICE_PUB_NAME);
        if (binder == null) {
            log("binder is null");
            return null;
        }
        try {
            binder.linkToDeath(mDeathRecipient, 0);
            mRcsService = IRCSService.Stub.asInterface(binder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return mRcsService;
    }

    /** This is our function to un-binds from our service. */
    public void release() {
        sInstance = null;
        log("release.");
    }

    /**
     * Check if the JioJoin application is registered on the network
     *
     * @param int subId
     * @return true if JioJoin is registered; false otherwise
     */
    public boolean isSessionRegistered(int subId) {
        log("isSessionRegistered");
        if (isServiceConnected()) {
            try {
                return getService().isSessionRegistered(subId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Allows to make a PostCall. Calling this method will launch JioJoin's
     * PostCall dialog; The dialog will present the User with two options,
     * "ignore" and "send message". If the User chooses to ignore, the dialog
     * will be dismissed, if the User chooses to "send message" an activity will
     * be shown with the options to compose a message and record an audio clip.
     *
     * @param phoneNumber, the phone number to make the call, int subId
     * @param callback, the callback defined on EnrichCallCallback which will return
     *            true if the PostCall dialog was created; false otherwise.
     *
     * @return boolean, returns true if function call is made to service, else false.
     * Note: Not getting used now.
     */
    public boolean makePostCall(String phoneNumber, INewPostCallCallback callback,
            int subId) {
        log("makePostCall");
        if (!canContinueWithReq(phoneNumber, callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().makePostCall(phoneNumber, callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Fetch capabilities for a specific number (dialled). Before making a
     * PostCall it's required that we first check if the callee has support for
     * this feature; otherwise it won't be possible to establish a session.
     *
     * @param phoneNumber, the phone number to make the call, int subId
     * @param callback, the callback defined on EnrichCallCallback which will return
     *            if the number dialled (@phoneNumber) supports PostCall or not.
     *
     * @return boolean, returns true if function call is made to service, else false.
     * Note: Not getting used now.
     */
    public boolean fetchPostCallCapabilities(String phoneNumber,
            PostCallCapabilitiesCallback callback, int subId) {
        log("fetchPostCallCapabilities");
        if (!canContinueWithReq(phoneNumber, callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().fetchPostCallCapabilities(phoneNumber, callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Subscription for enriched update. The subscription of this method is
     * required in order to receive updates about the current enriched call
     * session.
     *
     * @param callback, the callback defined on EnrichedCallUpdateCallback which
     *            will contain information about the current enriched call
     *            state, int subId.
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean subscribeEnrichedCallUpdate(EnrichedCallUpdateCallback callback,
            int subId) {
        log("subscribeEnrichedCallUpdate");
        if (!canContinueWithReq(callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().subscribeEnrichedCallUpdate(callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Unsubscription for enriched update.
     *
     * @param callback, the callback defined on EnrichedCallUpdateCallback which
     *            will contain information about the current enriched call
     *            state, int subId.
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean unsubscribeEnrichedCallUpdate(EnrichedCallUpdateCallback callback,
            int subId) {
        log("unsubscribeEnrichedCallUpdate");
        if (!canContinueWithReq(callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().unsubscribeEnrichedCallUpdate(callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Subscription for session state update. The subscription of this method is
     * required in order to receive updates about the registration state.
     *
     * @param callback, the callback defined on EnrichCallCallback which will
     *            contain information about the registration state, int subId.
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean subscribeSessionStateUpdate(SessionStateUpdateCallback callback,
            int subId) {
        log("subscribeSessionStateUpdate");
        if (!canContinueWithReq(callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().subscribeSessionStateUpdate(callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Unsubscription for session state update.
     *
     * @param callback, the callback defined on EnrichCallCallback which will
     *            contain information about the registration state, int subId.
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean unsubscribeSessionStateUpdate(SessionStateUpdateCallback callback,
            int subId) {
        log("unsubscribeSessionStateUpdate");
        if (!canContinueWithReq(callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().unsubscribeSessionStateUpdate(callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Subscription for incoming enriched calls. The subscription of this method
     * is required in order to receive a new enriched call.
     *
     * @param callback, the callback defined on IncomingEnrichedCallCallback which
     *            will contain information about the new enriched call, int
     *            subId.
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean subscribeIncomingEnrichedCall(IncomingEnrichedCallCallback callback,
            int subId) {
        log("subscribeIncomingEnrichedCall");
        if (!canContinueWithReq(callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().subscribeIncomingEnrichedCall(callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Unsubscription for incoming enriched calls.
     *
     * @param callback, the callback defined on IncomingEnrichedCallCallback which
     *            will contain information about the new enriched call, int
     *            subId.
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean unsubscribeIncomingEnrichedCall(IncomingEnrichedCallCallback callback,
            int subId) {
        log("unsubscribeIncomingEnrichedCall");
        if (!canContinueWithReq(callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().unsubscribeIncomingEnrichedCall(callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Allows to make an enriched call. Calling this method will launch JioJoin
     * call composer in order to enrich the current call; An activity will be
     * shown on a separated activity stack where the user has the option to add
     * text, image, location and define the call priority.
     *
     * @param phoneNumber, the phone number to make the call, callback the callback
     *            defined on NewCallComposerCallback which will return the
     *            CallComposerData, the object containing the call composer data
     *            (subject, image, location, priority), int subId.
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean makeEnrichedCall(String phoneNumber, NewCallComposerCallback callback,
            int subId) {
        log("makeEnrichedCall");
        if (!canContinueWithReq(phoneNumber, callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().makeEnrichedCall(phoneNumber, callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Fetch capabilities for a specific number (dialed). Before making an
     * enriched call it's required to first check if the callee has support for
     * this feature; otherwise it won't be possible to establish a RCS session.
     *
     * @param phoneNumber, the phone number to make the call
     * @param callback
     *            , the callback defined on RichCallCapabilitiesCallback which
     *            will return if the dialed number(@phoneNumber) supports rich
     *            calls or not, int subId.
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean fetchEnrichedCallCapabilities(String phoneNumber,
            RichCallCapabilitiesCallback callback, int subId) {
        log("fetchEnrichedCallCapabilities");
        if (!canContinueWithReq(phoneNumber, callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().fetchEnrichedCallCapabilities(phoneNumber, callback, subId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * get the map image from the google maps as a image. the download map image
     * will be replied by using FetchImageCallBack callback.
     *
     * @param double latitude, double longitude, int width of the image int
     *        height of the image, FetchImageCallBack callback when the image is
     *        downloaded then the result will be sent using FetchImageCallBack
     * @return boolean, returns true if function call is made to service, else false.
     */
    public boolean fetchStaticMap(double lat, double lon, int width, int height,
            FetchImageCallBack callback) {
        log("fetchStaticMap");
        if (!canContinueWithReq(callback)) {
            log(GENERIC_MESSAGE);
            return false;
        }
        try {
            getService().fetchStaticMap(lat, lon, width, height, callback);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This API will be used to know if the service is connected or not.
     *
     * @return boolean, true if service is connected else false.
     */
    public boolean isServiceConnected() {
        boolean status = getService() != null;
        log("isServiceConnected : " + status);
        return status;
    }

    /**
     * This API will be used to know if Enrich call is capable or not.
     *
     * @param int subId.
     * @return boolean, true if enrichcall are capable else false.
     */
    public boolean isEnrichedCallCapable(int subId) {
        boolean status = true;
        int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (!mIsQcomHardware || !mIsLYFProduct
                || !isRcsConfigEnabledonSub(dataSubId)
                || !isNetworkSupportedEnrichCall(dataSubId)) {
            log("non rcs capable device");
            status = false;
        }
        log("isEnrichedCallCapable : " + status);
        return status;
    }

    /**
     * This API will be used to know if Enrich call feature is enabled.
     *
     * @return boolean, true if enrichcall feature is enabled else false.
     */
    public boolean isEnrichCallFeatureEnabled() {
        return mIsRcsEnabled;
    }

    /**
     * This API will be used to know if Enrich call can be initialized on
     * perticular sub.
     *
     * @param int subscription ID
     * @return boolean, true if enrichcall can be initiated on perticular sub.
     *         else false.
     */
    public boolean isRcsConfigEnabledonSub(int subId) {
        log("isRcsConfigEnabledonSub");

        boolean isEnrichedCallCapable = false;
        if ((subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) && (mContext != null)) {
            int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
            log("isRcsConfigEnabledonSub data subid is " + dataSubId);
            if (subId == dataSubId) {

                CarrierConfigManager configMgr = (CarrierConfigManager) mContext
                        .getSystemService(Context.CARRIER_CONFIG_SERVICE);
                // Fail if we can't find the carrier config service.
                if (configMgr == null) {
                    log("failed to get ConfigManager service");
                    return isEnrichedCallCapable;
                }
                PersistableBundle carrierConfig = configMgr.getConfigForSubId(subId);
                // Fail if no carrier config found.
                if (carrierConfig == null) {
                    log("failed to get carrierConfig bundle");
                    return isEnrichedCallCapable;
                }
                isEnrichedCallCapable = carrierConfig.getBoolean(CONFIG_ENRICHED_CALL_CAPABLE);
            }
            log("isRcsConfigEnabledonSub value " + isEnrichedCallCapable);
        }
        return isEnrichedCallCapable;
    }

    /**
     * get the rcs application package name and the service clas name.
     *
     * @return ComponentName of the rcs service if present else null.
     */
    private ComponentName getRcsServiceComponent() {
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = new Intent(RCS_APP_ACTION);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(intent, 0);
        if (resolveInfos == null || resolveInfos.isEmpty()) {
            return null;
        }
        ServiceInfo serviceInfo = resolveInfos.get(0).serviceInfo;
        if (serviceInfo != null) {
            return new ComponentName(serviceInfo.packageName, serviceInfo.name);
        }
        return null;
    }

    /**
     * To know if the the caller has sent the valid param we will check if any
     * params are null. If any param is null then we will not continue.
     * This function firstly check if the RcsService is connected or not.
     * And it will only continue if service is connected, else returns
     * false.
     *
     * @return boolean, true if we can continue processing else false.
     */
    private boolean canContinueWithReq(Object... objs) {
        if (!isServiceConnected()) {
            return false;
        }
        if (objs != null) {
            for (Object obj : objs) {
                if (obj == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * isNetworkSupportedEnrichCall to know if the network support enrichcall.
     * in this case we will check if the current network type is LTE or LTE_CA
     * to know if network supports enrich calls.
     *
     * @param  int datasubscription id in int.
     * @return boolean, true if support else false.
     */
    private boolean isNetworkSupportedEnrichCall(int dataSubId) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        int currentNetworkType = telephonyManager.getNetworkType(dataSubId);
        log("network type = " + currentNetworkType);
        if ((TelephonyManager.NETWORK_TYPE_LTE == currentNetworkType)
                || (TelephonyManager.NETWORK_TYPE_LTE_CA == currentNetworkType)) {
            return true;
        }
        return false;
    }

    private void log(String msg) {
        if (DBG) {
            Log.d(TAG, msg);
        }
    }

    /**
     * Death recipient class for monitoring RCS service.
     */
    private class RcsServiceDeathRecipient implements IBinder.DeathRecipient {
        @Override
        public void binderDied() {
            log("binderDied");
            mRcsService = null;

            if (mContext != null) {
                initialize();
            }
        }
    }
}
