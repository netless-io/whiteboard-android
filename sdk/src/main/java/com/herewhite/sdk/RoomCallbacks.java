package com.herewhite.sdk;

import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

/**
 * Created by buhe on 2018/8/12.
 */

public interface RoomCallbacks {

    void onPhaseChanged(RoomPhase phase);

    void onBeingAbleToCommitChange(boolean isAbleToCommit);

    void onDisconnectWithError(Exception e);

    void onKickedWithReason(String reason);

    void onRoomStateChanged(RoomState modifyState);

    void onCatchErrorWhenAppendFrame(long userId, Exception error);
}
