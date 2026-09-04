package com.herewhite.sdk.domain;

import androidx.annotation.Nullable;

/** Structured acceptance result returned by dispatchDocsEvent. */
public class DispatchDocsEventResult extends WhiteObject {
    public static final String INVALID_EVENT = "invalidEvent";
    public static final String INVALID_OPTIONS = "invalidOptions";
    public static final String TARGET_NOT_FOUND = "targetNotFound";
    public static final String TARGET_NOT_SUPPORTED = "targetNotSupported";
    public static final String EVENT_NOT_SUPPORTED = "eventNotSupported";
    public static final String NOT_WRITABLE = "notWritable";
    public static final String STATE_UNAVAILABLE = "stateUnavailable";
    public static final String OUT_OF_RANGE = "outOfRange";
    public static final String COMMAND_FAILED = "commandFailed";

    private boolean accepted;
    @Nullable private String reason;
    @Nullable private String message;

    public boolean isAccepted() { return accepted; }
    @Nullable public String getReason() { return reason; }
    @Nullable public String getMessage() { return message; }
}
