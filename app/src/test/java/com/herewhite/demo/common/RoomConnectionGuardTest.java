package com.herewhite.demo.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.herewhite.sdk.domain.RoomPhase;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomConnectionGuardTest {
    private RecordingListener listener;
    private RoomConnectionGuard guard;

    @Before
    public void setUp() {
        listener = new RecordingListener();
        guard = new RoomConnectionGuard(listener);
    }

    @Test
    public void reconnectingShowsLoadingAndConnectedHidesIt() {
        assertTrue(guard.onJoinStarted());
        guard.onRoomPhaseChanged(RoomPhase.reconnecting);

        assertTrue(listener.loadingVisible);
        assertTrue(listener.reconnectDelays.isEmpty());

        guard.onRoomPhaseChanged(RoomPhase.connected);

        assertFalse(listener.loadingVisible);
        assertEquals(1, listener.cancelCount);
    }

    @Test
    public void terminalPhasesDoNotScheduleUnconfirmedRejoin() {
        guard.onJoinStarted();
        guard.onRoomPhaseChanged(RoomPhase.disconnecting);
        guard.onRoomPhaseChanged(RoomPhase.disconnected);
        assertTrue(listener.loadingVisible);
        assertTrue(listener.reconnectDelays.isEmpty());
        assertFalse(guard.onReconnectTimerFired());
    }

    @Test
    public void failedJoinDoesNotRetryAnOwnedHost() {
        guard.onJoinStarted(); guard.onJoinFailed();
        assertTrue(listener.reconnectDelays.isEmpty());
        assertFalse(guard.onReconnectTimerFired());
    }

    @Test
    public void activeLeaveBlocksFutureJoin() {
        guard.onJoinStarted(); guard.leaveRoom();
        guard.onRoomPhaseChanged(RoomPhase.disconnected);
        assertFalse(listener.loadingVisible);
        assertFalse(guard.onJoinStarted());
        assertTrue(listener.reconnectDelays.isEmpty());
    }

    private static final class RecordingListener implements RoomConnectionGuard.Listener {
        boolean loadingVisible;
        int cancelCount;
        final List<Long> reconnectDelays = new ArrayList<>();

        @Override
        public void onLoadingChanged(boolean visible) {
            loadingVisible = visible;
        }

        @Override
        public void onReconnectRequested(long delayMs) {
            reconnectDelays.add(delayMs);
        }

        @Override
        public void onReconnectCancelled() {
            cancelCount++;
        }
    }
}
