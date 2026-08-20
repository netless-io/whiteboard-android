package com.herewhite.demo.common;

import com.herewhite.sdk.domain.RoomPhase;

final class RoomConnectionGuard {
    static final long INITIAL_RETRY_DELAY_MS = 1_000L;
    static final long MAX_RETRY_DELAY_MS = 10_000L;

    interface Listener {
        void onLoadingChanged(boolean visible);

        void onReconnectRequested(long delayMs);

        void onReconnectCancelled();
    }

    private final Listener listener;
    private boolean loadingVisible;
    private boolean joinInProgress;
    private boolean reconnectScheduled;
    private boolean stopped;
    private int retryAttempt;

    RoomConnectionGuard(Listener listener) {
        this.listener = listener;
    }

    boolean onJoinStarted() {
        if (stopped) {
            return false;
        }
        joinInProgress = true;
        reconnectScheduled = false;
        setLoadingVisible(true);
        return true;
    }

    void onJoinSucceeded() {
        joinInProgress = false;
    }

    void onJoinFailed() {
        joinInProgress = false;
        setLoadingVisible(true);
        scheduleReconnect();
    }

    void onRoomPhaseChanged(RoomPhase phase) {
        if (stopped) {
            return;
        }
        switch (phase) {
            case connected:
                joinInProgress = false;
                retryAttempt = 0;
                cancelReconnect();
                setLoadingVisible(false);
                break;
            case connecting:
            case reconnecting:
                setLoadingVisible(true);
                break;
            case disconnecting:
            case disconnected:
                joinInProgress = false;
                setLoadingVisible(true);
                scheduleReconnect();
                break;
        }
    }

    boolean onReconnectTimerFired() {
        if (stopped || !reconnectScheduled) {
            return false;
        }
        reconnectScheduled = false;
        joinInProgress = true;
        return true;
    }

    void leaveRoom() {
        stopped = true;
        joinInProgress = false;
        cancelReconnect();
        setLoadingVisible(false);
    }

    void stop() {
        leaveRoom();
    }

    private void scheduleReconnect() {
        if (stopped || joinInProgress || reconnectScheduled) {
            return;
        }
        long delay = INITIAL_RETRY_DELAY_MS << retryAttempt;
        delay = Math.min(delay, MAX_RETRY_DELAY_MS);
        retryAttempt = Math.min(retryAttempt + 1, 4);
        reconnectScheduled = true;
        listener.onReconnectRequested(delay);
    }

    private void cancelReconnect() {
        if (reconnectScheduled) {
            reconnectScheduled = false;
        }
        listener.onReconnectCancelled();
    }

    private void setLoadingVisible(boolean visible) {
        if (loadingVisible == visible) {
            return;
        }
        loadingVisible = visible;
        listener.onLoadingChanged(visible);
    }
}
