package com.herewhite.sdk;

import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;

import org.junit.After;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

abstract class WhiteSdkIntegrationTestHost {
    private static final long JOIN_TIMEOUT_SECONDS = 45L;

    protected WhiteboardView whiteboardView;
    protected WhiteSdk sdk;
    protected Room room;

    @After
    public void tearDownHost() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            if (room != null) {
                room.disconnect(new Promise<Object>() {
                    @Override
                    public void then(Object o) {
                    }

                    @Override
                    public void catchEx(SDKError t) {
                    }
                });
            }
            if (sdk != null) {
                sdk.releaseRoom();
            }
            if (whiteboardView != null) {
                whiteboardView.destroy();
            }
        });
        SystemClock.sleep(300L);
        room = null;
        sdk = null;
        whiteboardView = null;
    }

    protected Room joinRoom(RoomListener roomListener) throws Throwable {
        TestCredentialProvider.RoomCredentials credentials = TestCredentialProvider.requireRoomCredentials();
        AtomicReference<Room> roomRef = new AtomicReference<>();
        AtomicReference<SDKError> errorRef = new AtomicReference<>();
        CountDownLatch joinLatch = new CountDownLatch(1);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            whiteboardView = new WhiteboardView(ApplicationProvider.getApplicationContext());
            whiteboardView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(credentials.appIdentifier, true);
            configuration.setUserCursor(true);
            sdk = new WhiteSdk(whiteboardView, ApplicationProvider.getApplicationContext(), configuration);

            RoomParams roomParams = new RoomParams(
                    credentials.roomUuid,
                    credentials.roomToken,
                    "android-sdk-test-" + System.currentTimeMillis()
            );
            roomParams.setDisableNewPencil(false);
            roomParams.setWritable(true);

            sdk.joinRoom(roomParams, roomListener, new Promise<Room>() {
                @Override
                public void then(Room room) {
                    roomRef.set(room);
                    joinLatch.countDown();
                }

                @Override
                public void catchEx(SDKError t) {
                    errorRef.set(t);
                    joinLatch.countDown();
                }
            });
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        if (!joinLatch.await(JOIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out joining room");
        }
        if (errorRef.get() != null) {
            throw new AssertionError("Join room failed: " + errorRef.get().getMessage());
        }

        room = roomRef.get();
        return room;
    }
}
