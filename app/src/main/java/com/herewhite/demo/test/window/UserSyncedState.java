package com.herewhite.demo.test.window;

import com.herewhite.sdk.SyncedStoreObject;

public class UserSyncedState extends SyncedStoreObject {
    public DragViewState dragViewState = new DragViewState();
}

class DragViewState extends SyncedStoreObject {
    // position
    public float w = 0;
    public float h = 0;
    public float offX = 0;
    public float offY = 0;

    // logic
    public long leftTime = 0;
}
