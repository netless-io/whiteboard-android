package com.herewhite.sdk.internal;


import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.RoomPhase;

/**
 * 内部接口，解耦jsBridgeInterface与Room
 */
public interface RoomDelegate {
    void fireCanUndoStepsUpdate(long valueOf);

    void onCanRedoStepsUpdate(long valueOf);

    void fireMagixEvent(EventEntry eventEntry);

    void fireHighFrequencyEvent(EventEntry[] events);

    void firePhaseChanged(RoomPhase valueOf);

    void fireKickedWithReason(String valueOf);

    void fireDisconnectWithError(Exception e);

    void fireRoomStateChanged(String valueOf);

    void fireCatchErrorWhenAppendFrame(long userId, Exception e);
}
