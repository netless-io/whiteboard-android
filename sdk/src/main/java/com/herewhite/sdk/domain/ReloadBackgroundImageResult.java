package com.herewhite.sdk.domain;

import org.json.JSONObject;

public final class ReloadBackgroundImageResult {
    public final boolean accepted;
    public final int reloadedCount;
    public final String reason;

    public ReloadBackgroundImageResult(JSONObject json) {
        accepted = json.optBoolean("accepted");
        reloadedCount = json.optInt("reloadedCount");
        reason = json.optString("reason", null);
    }
}
