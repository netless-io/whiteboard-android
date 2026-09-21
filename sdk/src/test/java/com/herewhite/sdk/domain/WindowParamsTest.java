package com.herewhite.sdk.domain;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class WindowParamsTest {
    @Test
    public void serializesUseBoxesStatus() throws Exception {
        WindowParams params = new WindowParams().setUseBoxesStatus(true);

        JSONObject json = params.toJSON();

        assertEquals(true, json.getBoolean("useBoxesStatus"));
    }

    @Test
    public void serializesForceMaximizedLazySetupAndCacheLimit() throws Exception {
        WindowParams params = new WindowParams()
                .setUseBoxesStatus(false)
                .setForceMaximized(true)
                .setLazySetupInMaximizedMode(true)
                .setMaxCachedAppsInMaximizedMode(3);

        JSONObject json = params.toJSON();

        assertEquals(false, json.getBoolean("useBoxesStatus"));
        assertEquals(true, json.getBoolean("forceMaximized"));
        assertEquals(true, json.getBoolean("lazySetupInMaximizedMode"));
        assertEquals(3, json.getInt("maxCachedAppsInMaximizedMode"));
    }

    @Test
    public void omitsUnsetLazySetupOptions() throws Exception {
        JSONObject json = new WindowParams().toJSON();

        assertFalse(json.has("forceMaximized"));
        assertFalse(json.has("lazySetupInMaximizedMode"));
        assertFalse(json.has("maxCachedAppsInMaximizedMode"));
    }

    @Test
    public void serializesOriginSizeAndPageScaleRange() throws Exception {
        WindowParams params = new WindowParams()
                .setOriginSize(new WindowOriginSize(1280, 900))
                .setPageScaleRange(new PageScaleRange().setMinScale(0.5).setMaxScale(4.0));

        JSONObject json = params.toJSON();
        assertEquals(1280.0, json.getJSONObject("originSize").getDouble("width"), 0.0);
        assertEquals(900.0, json.getJSONObject("originSize").getDouble("height"), 0.0);
        assertEquals(0.5, json.getJSONObject("pageScaleRange").getDouble("minScale"), 0.0);
        assertEquals(4.0, json.getJSONObject("pageScaleRange").getDouble("maxScale"), 0.0);
    }

    @Test
    public void windowAppOriginSizeUsesAttributesContract() throws Exception {
        WindowAppParam params = WindowAppParam.createSlideApp("/slide", new Scene[]{new Scene("1")}, "Slide")
                .setOriginSize(new WindowOriginSize(1280, 900));

        JSONObject json = params.getResolvedAttributes().toJSON();
        assertEquals(1280.0, json.getJSONObject("originSize").getDouble("width"), 0.0);
        assertEquals(900.0, json.getJSONObject("originSize").getDouble("height"), 0.0);
    }
}
