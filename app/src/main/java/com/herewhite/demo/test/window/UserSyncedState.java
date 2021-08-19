package com.herewhite.demo.test.window;

import com.herewhite.sdk.domain.SyncedState;

public class UserSyncedState extends SyncedState {
    public DragViewState dragViewState = new DragViewState();
}

class DragViewState extends SyncedState {
    // position
    public float w = 0;
    public float h = 0;
    public float offX = 0;
    public float offY = 0;

    // logic
    public long leftTime = 0;
}
