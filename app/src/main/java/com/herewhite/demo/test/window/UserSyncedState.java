package com.herewhite.demo.test.window;

import com.herewhite.sdk.domain.WhiteObject;

public class UserSyncedState extends WhiteObject {
    public DragViewState dragViewState = new DragViewState();
}

class DragViewState extends WhiteObject {
    // position
    public float w = 0;
    public float h = 0;
    public float offX = 0;
    public float offY = 0;

    // logic
    public long leftTime = 0;
}
