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
    public void terminalPhasesScheduleOnlyOneRoomRecreation() {
        guard.onJoinStarted();
        guard.onRoomPhaseChanged(RoomPhase.disconnecting);
        guard.onRoomPhaseChanged(RoomPhase.disconnected);

        assertEquals(Arrays.asList(1_000L), listener.reconnectDelays);
        assertTrue(guard.onReconnectTimerFired());
        assertFalse(guard.onReconnectTimerFired());
    }

    @Test
    public void repeatedJoinFailuresRetryForeverWithCappedBackoff() {
        long[] expectedDelays = {1_000L, 2_000L, 4_000L, 8_000L, 10_000L, 10_000L};

        for (long expectedDelay : expectedDelays) {
            guard.onJoinStarted();
            guard.onJoinFailed();
            assertEquals(expectedDelay,
                    (long) listener.reconnectDelays.get(listener.reconnectDelays.size() - 1));
            assertTrue(guard.onReconnectTimerFired());
        }
    }

    @Test
    public void connectedResetsBackoffAndCancelsPendingRecreation() {
        guard.onJoinStarted();
        guard.onJoinFailed();
        assertTrue(guard.onReconnectTimerFired());
        guard.onJoinStarted();
        guard.onJoinFailed();

        guard.onRoomPhaseChanged(RoomPhase.connected);
        guard.onRoomPhaseChanged(RoomPhase.disconnected);

        assertEquals(Arrays.asList(1_000L, 2_000L, 1_000L), listener.reconnectDelays);
    }

    @Test
    public void activeLeaveCancelsRetryAndBlocksFutureJoin() {
        guard.onJoinStarted();
        guard.onRoomPhaseChanged(RoomPhase.disconnected);

        guard.leaveRoom();
        guard.onRoomPhaseChanged(RoomPhase.disconnected);

        assertFalse(listener.loadingVisible);
        assertFalse(guard.onReconnectTimerFired());
        assertFalse(guard.onJoinStarted());
        assertEquals(1, listener.reconnectDelays.size());
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
