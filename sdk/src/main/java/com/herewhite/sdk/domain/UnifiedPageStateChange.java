package com.herewhite.sdk.domain;

import androidx.annotation.Nullable;

/** A unified page observation or terminal command failure. */
public class UnifiedPageStateChange extends UnifiedPageState {
    private String status;
    @Nullable private String changeType;
    @Nullable private Integer mainView;
    @Nullable private Integer presentation;
    @Nullable private Integer view;
    @Nullable private Integer slide;
    @Nullable private String event;
    @Nullable private String reason;
    @Nullable private String message;

    public String getStatus() { return status; }
    @Nullable public String getChangeType() { return changeType; }
    @Nullable public Integer getMainView() { return mainView; }
    @Nullable public Integer getPresentation() { return presentation; }
    @Nullable public Integer getView() { return view; }
    @Nullable public Integer getSlide() { return slide; }
    @Nullable public String getEvent() { return event; }
    @Nullable public String getReason() { return reason; }
    @Nullable public String getMessage() { return message; }
}
