package com.herewhite.sdk;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RoomParamsTest {
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
