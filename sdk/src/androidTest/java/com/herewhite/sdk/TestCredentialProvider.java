package com.herewhite.sdk;

import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

import com.herewhite.sdk.local.SdkTestDefaults;

import org.junit.Assume;

class TestCredentialProvider {
    private static final String APP_IDENTIFIER_ARG = "whiteboard.test.appIdentifier";
    private static final String ROOM_UUID_ARG = "whiteboard.test.roomUuid";
    private static final String ROOM_TOKEN_ARG = "whiteboard.test.roomToken";

    static RoomCredentials requireRoomCredentials() {
        RoomCredentials credentials = loadRoomCredentials();
        Assume.assumeTrue(
                "Missing test credentials. Pass instrumentation args or provide local SdkTestDefaults.",
                credentials != null
        );
        return credentials;
    }

    static RoomCredentials loadRoomCredentials() {
        String appIdentifier = firstNonEmpty(
                readArgument(APP_IDENTIFIER_ARG),
                SdkTestDefaults.DEFAULT_APP_IDENTIFIER
        );
        String roomUuid = firstNonEmpty(
                readArgument(ROOM_UUID_ARG),
                SdkTestDefaults.DEFAULT_ROOM_UUID
        );
        String roomToken = firstNonEmpty(
                readArgument(ROOM_TOKEN_ARG),
                SdkTestDefaults.DEFAULT_ROOM_TOKEN
        );
        if (isEmpty(appIdentifier) || isEmpty(roomUuid) || isEmpty(roomToken)) {
            return null;
        }
        return new RoomCredentials(appIdentifier, roomUuid, roomToken);
    }

    private static String readArgument(String key) {
        Bundle arguments = InstrumentationRegistry.getArguments();
        return arguments.getString(key);
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (!isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    static class RoomCredentials {
        final String appIdentifier;
        final String roomUuid;
        final String roomToken;

        RoomCredentials(String appIdentifier, String roomUuid, String roomToken) {
            this.appIdentifier = appIdentifier;
            this.roomUuid = roomUuid;
            this.roomToken = roomToken;
        }
    }
}
