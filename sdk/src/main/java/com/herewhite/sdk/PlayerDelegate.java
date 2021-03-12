package com.herewhite.sdk;

import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.SDKError;

public interface PlayerDelegate {
    void fireMagixEvent(EventEntry fromJson);

    void fireHighFrequencyEvent(EventEntry[] events);

    void setPlayerPhase(PlayerPhase phase);

    void onLoadFirstFrame();

    void onSliceChanged(String valueOf);

    void syncDisplayerState(String valueOf);

    void onStoppedWithError(SDKError resolverSDKError);

    void setScheduleTime(long scheduleTime);

    void onCatchErrorWhenAppendFrame(SDKError resolverSDKError);

    void onCatchErrorWhenRender(SDKError resolverSDKError);
}
