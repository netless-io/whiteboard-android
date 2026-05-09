package com.herewhite.sdk;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.platform.app.InstrumentationRegistry;

import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class RoomIntegrationTest extends WhiteSdkIntegrationTestHost {
    @Test
    public void setMemberState_triggersRoomStateChangedCallback() throws Throwable {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch memberStateChangedLatch = new CountDownLatch(1);
        AtomicReference<RoomState> roomStateRef = new AtomicReference<>();
        AtomicReference<String> expectedApplianceRef = new AtomicReference<>(Appliance.TEXT);

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
                MemberState changedMemberState = modifyState == null ? null : modifyState.getMemberState();
                if (changedMemberState != null
                        && expectedApplianceRef.get().equals(changedMemberState.getCurrentApplianceName())) {
                    roomStateRef.set(modifyState);
                    memberStateChangedLatch.countDown();
                }
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

        MemberState memberState = new MemberState();
        memberState.setCurrentApplianceName(Appliance.TEXT);
        memberState.setStrokeColor(new int[]{12, 34, 56});
        memberState.setStrokeWidth(6d);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> room.setMemberState(memberState));

        if (!memberStateChangedLatch.await(20, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for onRoomStateChanged");
        }

        RoomState roomState = roomStateRef.get();
        assertNotNull(roomState);
        MemberState updatedState = roomState.getMemberState();
        assertNotNull(updatedState);
        assertEquals(Appliance.TEXT, updatedState.getCurrentApplianceName());
        assertArrayEquals(new int[]{12, 34, 56}, updatedState.getStrokeColor());
        assertEquals(6d, updatedState.getStrokeWidth(), 0.001d);
    }
}
