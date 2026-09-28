package com.herewhite.demo.test.window;

import com.herewhite.sdk.WhiteSdkConfiguration;

/** Uses the same demo configuration and controls, with only AppliancePlugin disabled. */
public class WindowNoAppliancePluginActivity extends WindowAppliancePluginActivity {

    @Override
    protected WhiteSdkConfiguration generateSdkConfig() {
        WhiteSdkConfiguration configuration = super.generateSdkConfig();
        configuration.setEnableAppliancePlugin(false);
        return configuration;
    }
}
