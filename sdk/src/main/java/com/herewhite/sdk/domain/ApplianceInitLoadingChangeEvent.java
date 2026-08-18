package com.herewhite.sdk.domain;

import org.json.JSONObject;

/** Appliance plugin initialization progress reported by the Web runtime. */
public final class ApplianceInitLoadingChangeEvent {
    public final String name;
    public final boolean loading;
    public final String phase;
    public final String status;

    public ApplianceInitLoadingChangeEvent(JSONObject json) {
        name = json.optString("name");
        loading = json.optBoolean("loading");
        phase = json.optString("phase");
        status = json.optString("status");
    }
}
