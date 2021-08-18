package com.herewhite.demo;

import com.herewhite.sdk.domain.SyncedState;

public class UserSyncedState extends SyncedState {
    public ClockState clockState = new ClockState();
    public InterUserSyncedState interClass = new InterUserSyncedState();
}

class InterUserSyncedState extends SyncedState {
    public String aStr = "aStr";
    public Long aLong = 100L;
    public Float aFloat = 1.1f;

    public String interStr = "interStr";
}

class ClockState extends SyncedState {
    public float w = 0;
    public float h = 0;
    public float offX = 0;
    public float offY = 0;
}
