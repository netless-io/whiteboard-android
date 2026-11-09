package com.herewhite.sdk;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
public class PreloadGateTest {
    @Test public void frozenSerializationPreservesIndependentDefaultOffGates() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app");
        assertFalse(config.hasOptionalPreload());
        assertFalse(config.toJSON().getBoolean("enablePreLoadAppliancePlugin"));
        config.setEnablePreLoadAppliancePlugin(true);
        assertFalse(config.hasOptionalPreload());
        config.setEnableAppliancePlugin(true);
        assertTrue(config.hasOptionalPreload());
        WhiteSdkConfiguration copy = Utils.deepCopy(config, WhiteSdkConfiguration.class);
        config.setEnablePreLoadAppliancePlugin(false);
        assertTrue(copy.hasOptionalPreload());
        assertTrue(copy.toJSON().getBoolean("enablePreLoadAppliancePlugin"));
        assertFalse(config.hasOptionalPreload());
        config.setPreloadBuiltinApps(Collections.emptyList()); assertFalse(config.hasOptionalPreload());
        config.setPreloadBuiltinApps(Arrays.asList("Slide")); assertTrue(config.hasOptionalPreload());
        config.setPreloadBuiltinApps(null); config.setEnableModelPreload(true); assertTrue(config.hasOptionalPreload());
        config.setEnableModelPreload(false); config.setDynamicConfig(Collections.emptyMap()); assertTrue(config.hasOptionalPreload());
    }
}
