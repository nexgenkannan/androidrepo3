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

import android.net.Uri;
import org.codeaurora.rcscommon.INewPostCallCallback;
import org.codeaurora.rcscommon.PostCallCapabilitiesCallback;
import org.codeaurora.rcscommon.EnrichedCallUpdateCallback;
import org.codeaurora.rcscommon.SessionStateUpdateCallback;
import org.codeaurora.rcscommon.IncomingEnrichedCallCallback;
import org.codeaurora.rcscommon.NewCallComposerCallback;
import org.codeaurora.rcscommon.RichCallCapabilitiesCallback;
import org.codeaurora.rcscommon.FetchImageCallBack;

interface IRCSService {
    /**
     * Check if the JioJoin application is registered on the network
     *
     * @param int subId
     * @return true if JioJoin is registered; false otherwise
     */
    boolean isSessionRegistered(int subId);

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
     * Note: Not getting used now.
     */
    oneway void makePostCall(String phoneNumber, INewPostCallCallback callback, int subId);


    /**
     * Fetch capabilities for a specific number (dialled). Before making a
     * PostCall it's required that we first check if the callee has support for
     * this feature; otherwise it won't be possible to establish a session.
     *
     * @param phoneNumber, the phone number to make the call, int subId
     * @param callback, the callback defined on EnrichCallCallback which will return
     *            if the number dialled (@phoneNumber) supports PostCall or not.
     *
     * Note: Not getting used now.
     */
    oneway void fetchPostCallCapabilities(String phoneNumber, PostCallCapabilitiesCallback callback,
            int subId);

    /**
     * Subscription for enriched update. The subscription of this method is
     * required in order to receive updates about the current enriched call
     * session.
     *
     * @param callback, the callback defined on EnrichedCallUpdateCallback which
     *            will contain information about the current enriched call
     *            state, int subId.
     */
    oneway void subscribeEnrichedCallUpdate(EnrichedCallUpdateCallback callback, int subId);

    /**
     * Unsubscription for enriched update.
     *
     * @param callback, the callback defined on EnrichedCallUpdateCallback which
     *            will contain information about the current enriched call
     *            state, int subId.
     */
    oneway void unsubscribeEnrichedCallUpdate(EnrichedCallUpdateCallback callback, int subId);

    /**
     * Subscription for session state update. The subscription of this method is
     * required in order to receive updates about the registration state.
     *
     * @param callback, the callback defined on EnrichCallCallback which will
     *            contain information about the registration state, int subId.
     */
    oneway void subscribeSessionStateUpdate(SessionStateUpdateCallback callback, int subId);

    /**
     * Unsubscription for session state update.
     *
     * @param callback, the callback defined on EnrichCallCallback which will
     *            contain information about the registration state, int subId.
     */
    oneway void unsubscribeSessionStateUpdate(SessionStateUpdateCallback callback, int subId);

    /**
     * Subscription for incoming enriched calls. The subscription of this method
     * is required in order to receive a new enriched call.
     *
     * @param callback, the callback defined on IncomingEnrichedCallCallback which
     *            will contain information about the new enriched call, int
     *            subId.
     */
    oneway void subscribeIncomingEnrichedCall(IncomingEnrichedCallCallback callback, int subId);

    /**
     * Unsubscription for incoming enriched calls.
     *
     * @param callback, the callback defined on IncomingEnrichedCallCallback which
     *            will contain information about the new enriched call, int
     *            subId.
     */
    oneway void unsubscribeIncomingEnrichedCall(IncomingEnrichedCallCallback callback, int subId);

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
     */
    oneway void makeEnrichedCall(String phoneNumber, NewCallComposerCallback callback, int subId);

    /**
     * Fetch capabilities for a specific number (dialed). Before making an
     * enriched call it's required to first check if the callee has support for
     * this feature; otherwise it won't be possible to establish a session.
     *
     * @param phoneNumber, the phone number to make the call
     * @param callback
     *            , the callback defined on RichCallCapabilitiesCallback which
     *            will return if the dialed number(@phoneNumber) supports rich
     *            calls or not, int subId.
     */
    oneway void fetchEnrichedCallCapabilities(String phoneNumber,
            RichCallCapabilitiesCallback callback, int subId);

    /**
     * get the map image from the google maps as a image. the download map image
     * will be replied by using FetchImageCallBack callback.
     *
     * @param double latitude, double longitude, int width of the image int
     *        height of the image, FetchImageCallBack callback when the image is
     *        downloaded then the result will be sent using FetchImageCallBack
     */
    oneway void fetchStaticMap(double lat, double lon, int width, int height,
            FetchImageCallBack callback);

}
