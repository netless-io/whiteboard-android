package com.herewhite.sdk.domain;

import org.json.JSONObject;

/** Single completion of the optional tasks enabled by the SDK constructor. */
public final class WhitePreloadResult {
    private final String state;
    private final String message;

    public WhitePreloadResult(JSONObject payload) {
        state = payload.opt("state") instanceof String ? (String) payload.opt("state") : null;
        message = payload.opt("message") instanceof String ? (String) payload.opt("message") : null;
    }

    /** success or warn. This callback does not report room readiness. */
    public String getState() { return state; }
    public String getMessage() { return message; }
    public boolean isValid() {
        return ("success".equals(state) && "".equals(message)) ||
                ("warn".equals(state) && message != null && !message.trim().isEmpty());
    }
}
