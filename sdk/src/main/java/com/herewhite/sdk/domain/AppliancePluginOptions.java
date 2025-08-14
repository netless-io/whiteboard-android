package com.herewhite.sdk.domain;

import java.util.Map;

public class AppliancePluginOptions extends WhiteObject {
    private Map<String, Object> extras;

    public AppliancePluginOptions() {}

    public Map<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }
}
