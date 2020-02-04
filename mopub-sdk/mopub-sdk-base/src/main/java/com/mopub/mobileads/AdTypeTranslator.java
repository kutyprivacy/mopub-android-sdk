// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.common.AdFormat;
import com.mopub.common.AdType;
import com.mopub.common.Preconditions;
import com.mopub.common.util.ResponseHeader;

import org.json.JSONObject;

import static com.mopub.network.HeaderUtils.extractHeader;
import static com.mopub.network.HeaderUtils.extractIntegerHeader;

public class AdTypeTranslator {
    public enum CustomEventType {
        // "Special" custom events that we let people choose in the UI.
        GOOGLE_PLAY_SERVICES_BANNER("admob_native_banner",
                "com.mopub.mobileads.GooglePlayServicesBanner", false),
        GOOGLE_PLAY_SERVICES_INTERSTITIAL("admob_full_interstitial",
                "com.mopub.mobileads.GooglePlayServicesInterstitial", false),

        // MoPub-specific custom events.
        MRAID_BANNER("mraid_banner",
                "com.mopub.mraid.MraidBanner", true),
        MRAID_INTERSTITIAL("mraid_interstitial",
                "com.mopub.mraid.MraidInterstitial", true),
        HTML_BANNER("html_banner",
                "com.mopub.mobileads.HtmlBanner", true),
        HTML_INTERSTITIAL("html_interstitial",
                "com.mopub.mobileads.HtmlInterstitial", true),
        VAST_VIDEO_INTERSTITIAL("vast_interstitial",
                "com.mopub.mobileads.VastVideoInterstitial", true),
        VAST_VIDEO_INTERSTITIAL_TWO("vast_interstitial_two",
                "com.mopub.mobileads.VastVideoInterstitialTwo", true),
        MOPUB_NATIVE("mopub_native",
                "com.mopub.nativeads.MoPubCustomEventNative", true),
        MOPUB_VIDEO_NATIVE("mopub_video_native",
                "com.mopub.nativeads.MoPubCustomEventVideoNative", true),
        MOPUB_REWARDED_VIDEO("rewarded_video",
                "com.mopub.mobileads.MoPubRewardedVideo", true),
        MOPUB_REWARDED_VIDEO_TWO("rewarded_video_two",
                "com.mopub.mobileads.MoPubRewardedVideoTwo", true),
        MOPUB_REWARDED_PLAYABLE("rewarded_playable",
                "com.mopub.mobileads.MoPubRewardedPlayable", true),

        UNSPECIFIED("", null, false);

        @NonNull
        private final String mKey;
        @Nullable
        private final String mClassName;
        private final boolean mIsMoPubSpecific;

        private CustomEventType(String key, String className, boolean isMoPubSpecific) {
            mKey = key;
            mClassName = className;
            mIsMoPubSpecific = isMoPubSpecific;
        }

        private static CustomEventType fromString(@Nullable final String key) {
            for (CustomEventType customEventType : values()) {
                if (customEventType.mKey.equals(key)) {
                    return customEventType;
                }
            }

            return UNSPECIFIED;
        }

        private static CustomEventType fromClassName(@Nullable final String className) {
            for (CustomEventType customEventType : values()) {
                if (customEventType.mClassName != null
                        && customEventType.mClassName.equals(className)) {
                    return customEventType;
                }
            }

            return UNSPECIFIED;
        }

        @Override
        public String toString() {
            return mClassName;
        }

        public static boolean isMoPubSpecific(@Nullable final String className) {
            return fromClassName(className).mIsMoPubSpecific;
        }
    }

    public static final String BANNER_SUFFIX = "_banner";
    public static final String INTERSTITIAL_SUFFIX = "_interstitial";
    private static final int NEW_VAST_PLAYER_FLAG = 2;  // 2 is the value for the AndroidX player
    private static final String NEW_VAST_PLAYER_SUFFIX = "_two";

    static String getAdNetworkType(String adType, String fullAdType) {
        String adNetworkType = AdType.INTERSTITIAL.equals(adType) ? fullAdType : adType;
        return adNetworkType != null ? adNetworkType : "unknown";
    }

    public static String getCustomEventName(@NonNull AdFormat adFormat,
            @NonNull String adType,
            @Nullable String fullAdType,
            @Nullable JSONObject headers) {
        Preconditions.checkNotNull(adFormat);
        Preconditions.checkNotNull(adType);

        // Logic to use new or old player
        final int vastPlayerVersion = extractIntegerHeader(headers,
                ResponseHeader.VAST_VIDEO_PLAYER_VERSION,
                1);
        final String versionSuffix = NEW_VAST_PLAYER_FLAG == vastPlayerVersion
                ? NEW_VAST_PLAYER_SUFFIX
                : "";

        switch (adType.toLowerCase()) {
            case AdType.CUSTOM:
                return extractHeader(headers, ResponseHeader.CUSTOM_EVENT_NAME);
            case AdType.STATIC_NATIVE:
                return CustomEventType.MOPUB_NATIVE.toString();
            case AdType.VIDEO_NATIVE:
                return CustomEventType.MOPUB_VIDEO_NATIVE.toString();
            case AdType.REWARDED_VIDEO:
                return CustomEventType.fromString(adType + versionSuffix).toString();
            case AdType.REWARDED_PLAYABLE:
                return CustomEventType.MOPUB_REWARDED_PLAYABLE.toString();
            case AdType.HTML:
            case AdType.MRAID:
                return (AdFormat.INTERSTITIAL.equals(adFormat)
                        ? CustomEventType.fromString(adType + INTERSTITIAL_SUFFIX)
                        : CustomEventType.fromString(adType + BANNER_SUFFIX)).toString();
            case AdType.INTERSTITIAL:
                return CustomEventType.fromString(fullAdType + INTERSTITIAL_SUFFIX
                        + versionSuffix).toString();
            default:
                return CustomEventType.fromString(adType + BANNER_SUFFIX).toString();
        }
    }
}
