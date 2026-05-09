package com.herewhite.sdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import androidx.test.platform.app.InstrumentationRegistry;

import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.Scene;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DisplayerIntegrationTest extends WhiteSdkIntegrationTestHost {
    @Test
    public void getEntireScenes_returnsSceneMap() throws Throwable {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        RoomListener roomListener = new RoomListener() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                if (phase == RoomPhase.connected) {
                    connectedLatch.countDown();
                }
            }

            @Override
            public void onDisconnectWithError(Exception e) {
            }

            @Override
            public void onKickedWithReason(String reason) {
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
            }

            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
            }
        };

        Room room = joinRoom(roomListener);
        if (!connectedLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Room did not reach connected phase");
        }

        AtomicReference<Map<String, Scene[]>> scenesRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        CountDownLatch scenesLatch = new CountDownLatch(1);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.getEntireScenes(new com.herewhite.sdk.domain.Promise<Map<String, Scene[]>>() {
            @Override
            public void then(Map<String, Scene[]> scenes) {
                scenesRef.set(scenes);
                scenesLatch.countDown();
            }

            @Override
            public void catchEx(com.herewhite.sdk.domain.SDKError t) {
                errorRef.set(new AssertionError("getEntireScenes failed: " + t.getMessage()));
                scenesLatch.countDown();
            }
        }));

        if (!scenesLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for getEntireScenes");
        }
        if (errorRef.get() != null) {
            throw new AssertionError(errorRef.get().getMessage(), errorRef.get());
        }

        Map<String, Scene[]> scenes = scenesRef.get();
        assertNotNull(scenes);
        assertFalse(scenes.isEmpty());
    }
}
