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

import android.os.Bundle;
import android.os.Parcelable;
import android.os.Parcel;
import android.os.SystemProperties;
import android.net.Uri;
import android.text.TextUtils;
import java.util.Objects;

/**
 * CallComposerData is a Parcelable class and has the same info as jio
 * CallComposerData. This class contains the rcs call info.
 */
public class CallComposerData implements Parcelable {

    // these following keys will be used for parcel and deparcel.
    private static final String PHONENUMBER = "phonenumber";
    private static final String SUBJECT = "subject";
    private static final String IMAGE_URI = "image_uri";
    private static final String LOCATION_LAT = "location_lat";
    private static final String LOCATION_LON = "location_lon";
    private static final String PRIORITY_TYPE = "priority";
    private static final String CALL_STATE = "call_state";

    /* String containing callee phone number */
    private String mPhoneNumber;
    /* Enriched call text */
    private String mSubject;
    /* Enriched call image Uri (location on file system) */
    private Uri mImage;
    /* Enriched call latitude */
    private double mLocationLatitude = -1d;
    /* Enriched call longitude */
    private double mLocationLongitude = -1d;

    /*
     * Enriched call priority types. NORMAL - normal priority HIGH - urgent call
     */
    public enum PRIORITY {
        NORMAL, HIGH
    }

    /* Enrich call priority */

    private EnrichedCallState mCallState = EnrichedCallState.UNKNOWN;
    private PRIORITY mPriority;

    /**
     * Create CallComposerData by reading the bundle content
     */
    public CallComposerData(Bundle bundle) {
        if (bundle != null) {
            this.mPhoneNumber = bundle.getString(PHONENUMBER);
            this.mSubject = bundle.getString(SUBJECT);
            this.mImage = bundle.getString(IMAGE_URI) != null
                    && !bundle.getString(IMAGE_URI).equals("null") ? Uri.parse(bundle
                    .getString(IMAGE_URI)) : null;
            this.mLocationLatitude = bundle.getDouble(LOCATION_LAT);
            this.mLocationLongitude = bundle.getDouble(LOCATION_LON);
            this.mPriority = bundle.getInt(PRIORITY_TYPE) == 0 ? PRIORITY.NORMAL : PRIORITY.HIGH;
            this.mCallState = TextUtils.isEmpty(bundle.getString(CALL_STATE))
                    ? EnrichedCallState.UNKNOWN
                    : EnrichedCallState.valueOf(bundle.getString(CALL_STATE));
        }
    }

    /**
     * Create CallComposerData using constructor params.
     */
    public CallComposerData(String phoneNumber, String subject, Uri imageUri, double lat,
            double lon, PRIORITY priority, byte[] locationImageArray) {
        this.mPhoneNumber = phoneNumber;
        this.mSubject = subject;
        this.mImage = imageUri;
        this.mLocationLatitude = lat;
        this.mLocationLongitude = lon;
        this.mPriority = priority;
    }

    // "De-parcel object
    private CallComposerData(Parcel in) {
        mPhoneNumber = in.readString();
        mSubject = in.readString();
        mImage = Uri.parse(in.readString());
        mLocationLatitude = in.readDouble();
        mLocationLongitude = in.readDouble();
        int priority = in.readInt();
        mPriority = priority == 0 ? PRIORITY.NORMAL : PRIORITY.HIGH;
        mCallState = in.readParcelable(EnrichedCallState.class.getClassLoader());
    }

    /**
     * get the Phone number
     *
     * @return phonenumber string
     */
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    /**
     * get the RCS subject
     *
     * @return subject string
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * get the image path URI number
     *
     * @return Image URI
     */
    public Uri getImageUri() {
        return mImage;
    }

    /**
     * get location longitude
     *
     * @return double longitude value
     */
    public double getLongitude() {
        return mLocationLongitude;
    }

    /**
     * get location latitude
     *
     * @return double lotitude value
     */
    public double getLatitude() {
        return mLocationLatitude;
    }

    /**
     * get Priority value
     *
     * @return PRIORITY enum
     */
    public PRIORITY getPriority() {
        return mPriority;
    }

    /**
     * set call state
     *
     * @param int call state(WAITING, ESTABLISHED, FAILED, UNKNOWN)
     * @return void
     */
    public void setCallState(EnrichedCallState state) {
        mCallState = state;
    }

    /**
     * get call state
     *
     * @return int call state
     */
    public EnrichedCallState getCallState() {
        return mCallState;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPhoneNumber);
        dest.writeString(mSubject);
        dest.writeString(mImage + "");
        dest.writeDouble(mLocationLatitude);
        dest.writeDouble(mLocationLongitude);
        dest.writeInt(mPriority == PRIORITY.NORMAL ? 0 : 1);
        dest.writeParcelable(mCallState, flags);
    }

    // Creator
    public static final Parcelable.Creator<CallComposerData> CREATOR = new Parcelable.Creator() {
        public CallComposerData createFromParcel(Parcel in) {
            return new CallComposerData(in);
        }

        public CallComposerData[] newArray(int size) {
            return new CallComposerData[size];
        }
    };

    /**
     * check if both the objects are having same content.
     *
     * @param CallComposerData object which need to be compared to
     * @return boolean true if both are equal else false
     */
    public boolean equals(CallComposerData compare) {
        if (compare == null) {
            return false;
        }

        return Objects.equals(getPhoneNumber(), compare.getPhoneNumber())
                && Objects.equals(getSubject(), compare.getSubject())
                && Objects.equals(getImageUri() + "", compare.getImageUri() + "")
                && (getLatitude() == compare.getLatitude())
                && (getLongitude() == compare.getLongitude())
                && (getPriority() == compare.getPriority())
                && (getCallState() == compare.getCallState());
    }

    /**
     * get callcomposerdata bundle
     *
     * @return Bundle
     */
    public Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(PHONENUMBER, getPhoneNumber());
        bundle.putString(SUBJECT, getSubject());
        bundle.putString(IMAGE_URI, getImageUri() + "");
        bundle.putDouble(LOCATION_LAT, getLatitude());
        bundle.putDouble(LOCATION_LON, getLongitude());
        bundle.putInt(PRIORITY_TYPE, getPriority() == PRIORITY.NORMAL ? 0 : 1);
        bundle.putString(CALL_STATE, getCallState().name());
        return bundle;
    }

    /**
     * isvalid to know if content of callcomposerdata is valid or not
     *
     * @return boolean true if valid else false.
     */
    public boolean isValid() {
        return getPhoneNumber() != null && getPhoneNumber().length() > 0
                && getCallState() != EnrichedCallState.FAILED;
    }

    /**
     * isValidLocation to know if location of callcomposerdata is valid or not
     *
     * @return boolean true if valid else false.
     */
    public boolean isValidLocation() {
        return (getLatitude() != -1d && getLongitude() != -1d)
                && (getLatitude() != 0d && getLongitude() != 0d);
    }

    /**
     * isValidSharedImageUri to know if shared image of callcomposerdata is
     * valid or not
     *
     * @return boolean true if valid else false.
     */
    public boolean isValidSharedImageUri() {
        return getImageUri() != null && getImageUri().toString().length() > 0;
    }

    /**
     * tostring to print all the value of callcomposerdata.
     *
     * @return String
     */
    public String toString() {
        return ((SystemProperties.getInt("ro.debuggable", 0) == 1) ?
                ("PhoneNumber: " + getPhoneNumber()) : "")
                + " Subject: "
                + getSubject()
                + " image: "
                + getImageUri()
                + " Latitude: "
                + getLatitude()
                + " Longitude: "
                + getLongitude()
                + " Priority: "
                + getPriority()
                + " CallState: "
                + getCallState();
    }

}
