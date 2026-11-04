package com.herewhite.sdk;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RoomParamsTest {
    @Test
    public void appPreloadSelectionSurvivesSdkJsonCopy() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app");
        assertFalse(config.toJSON().has("preloadBuiltinApps"));
        java.util.List<String> kinds = new java.util.ArrayList<>(java.util.Arrays.asList("Slide", "Presentation"));
        config.setPreloadBuiltinApps(kinds);
        kinds.clear();
        WhiteSdkConfiguration copy = Utils.deepCopy(config, WhiteSdkConfiguration.class);
        assertEquals("[\"Slide\",\"Presentation\"]", copy.toJSON().getJSONArray("preloadBuiltinApps").toString());
        config.getPreloadBuiltinApps().clear();
        assertEquals(2, config.getPreloadBuiltinApps().size());
        config.setPreloadBuiltinApps(java.util.Collections.emptyList());
        assertEquals(0, Utils.deepCopy(config, WhiteSdkConfiguration.class).toJSON().getJSONArray("preloadBuiltinApps").length());
        config.setPreloadBuiltinApps(null);
        assertFalse(config.toJSON().has("preloadBuiltinApps"));
        assertFalse(config.isEnableModelPreload());
    }

    @Test
    public void modelPreloadOptInSurvivesSdkJsonCopy() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app");
        assertFalse(config.isEnableModelPreload());
        assertFalse(config.toJSON().getBoolean("enableModelPreload"));
        config.setEnableModelPreload(true);
        WhiteSdkConfiguration copy = Utils.deepCopy(config, WhiteSdkConfiguration.class);
        org.junit.Assert.assertTrue(copy.isEnableModelPreload());
        org.junit.Assert.assertTrue(copy.toJSON().getBoolean("enableModelPreload"));
        config.setEnableModelPreload(false);
        assertFalse(Utils.deepCopy(config, WhiteSdkConfiguration.class).toJSON().getBoolean("enableModelPreload"));
    }

    @Test
    public void serializesUndoCacheScenesCountOnlyWhenConfigured() throws Exception {
        RoomParams params = new RoomParams("room-uuid", "room-token", "uid");

        assertFalse(params.toJSON().has("undoCacheScenesCount"));

        params.setUndoCacheScenesCount(32);
        JSONObject json = params.toJSON();

        assertEquals(Integer.valueOf(32), params.getUndoCacheScenesCount());
        assertEquals(32, json.getInt("undoCacheScenesCount"));
    }

    @Test
    public void preservesUnsetAndExplicitFalseSourcesAcrossJson() throws Exception {
        RoomParams params = new RoomParams("uuid", "token", "uid");
        JSONObject defaults = params.toJSON();
        org.junit.Assert.assertTrue(defaults.getJSONArray("__nativeConfigDefaults").toString().contains("floatBar"));
        params.setUsingFloatBar(false);
        params.setDisableOperations(false);
        JSONObject explicit = params.toJSON();
        assertFalse(explicit.getJSONArray("__nativeConfigDefaults").toString().contains("floatBar"));
        assertFalse(explicit.getJSONArray("__nativeConfigDefaults").toString().contains("disableDeviceInputs"));
        assertFalse(explicit.getJSONArray("__nativeConfigDefaults").toString().contains("disableCameraTransform"));
        assertFalse(explicit.getBoolean("floatBar"));
    }

    @Test
    public void serializesRegionUrlsAndPreservesSourcesInSdkCopy() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app");
        java.util.Map<String, String> urls = new java.util.HashMap<>();
        urls.put("cn-hz", "https://example.com/android/latest.json");
        urls.put("sg", "https://sg.example.com/android/latest.json");
        config.setDynamicConfig(urls);
        urls.clear();
        WhiteSdkConfiguration copy = Utils.deepCopy(config, WhiteSdkConfiguration.class);
        JSONObject json = copy.toJSON();
        assertEquals("https://sg.example.com/android/latest.json", json.getJSONObject("dynamicConfig").getString("sg"));
        assertEquals(2, json.getJSONObject("dynamicConfig").length());
        org.junit.Assert.assertTrue(json.getJSONArray("__nativeConfigDefaults").toString().contains("useMultiViews"));
        config.setUseMultiViews(false);
        copy = Utils.deepCopy(config, WhiteSdkConfiguration.class);
        assertFalse(copy.toJSON().getJSONArray("__nativeConfigDefaults").toString().contains("useMultiViews"));
        config.getDynamicConfig().clear();
        assertEquals(2, config.getDynamicConfig().size());
        config.setDynamicConfig(null);
        assertFalse(config.toJSON().has("dynamicConfig"));
    }
}
