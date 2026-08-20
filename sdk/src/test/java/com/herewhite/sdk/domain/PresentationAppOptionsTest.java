package com.herewhite.sdk.domain;

import com.herewhite.sdk.WhiteSdkConfiguration;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PresentationAppOptionsTest {
    @Test
    public void serializesPresentationOptionsUnderSdkConfiguration() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app-id");
        config.setPresentationAppOptions(new PresentationAppOptions()
                .setUseScrollbar(true)
                .setDebounceSync(false));

        JSONObject json = config.toJSON().getJSONObject("presentationAppOptions");

        assertEquals(true, json.getBoolean("useScrollbar"));
        assertEquals(false, json.getBoolean("debounceSync"));
    }

    @Test
    public void serializesEnableScaleUnderSlideAppOptions() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app-id");
        WhiteSdkConfiguration.SlideAppOptions slideAppOptions =
                new WhiteSdkConfiguration.SlideAppOptions();
        slideAppOptions.setEnableScale(true);
        config.setSlideAppOptions(slideAppOptions);

        JSONObject json = config.toJSON().getJSONObject("slideAppOptions");

        assertEquals(true, json.getBoolean("enableScale"));
    }

    @Test
    public void omitsUnsetSlidePerformanceOptions() throws Exception {
        JSONObject json = new WhiteSdkConfiguration("app-id")
                .toJSON()
                .getJSONObject("slideAppOptions");

        assertFalse(json.has("minFPS"));
        assertFalse(json.has("maxFPS"));
        assertFalse(json.has("resolution"));
        assertFalse(json.has("maxResolutionLevel"));
    }

    @Test
    public void serializesExplicitSlidePerformanceOptions() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app-id");
        WhiteSdkConfiguration.SlideAppOptions options = config.getSlideAppOptions();
        options.setMinFPS(10);
        options.setMaxFPS(20);
        options.setResolution(1.5);
        options.setMaxResolutionLevel(3);

        JSONObject json = config.toJSON().getJSONObject("slideAppOptions");

        assertEquals(10, json.getInt("minFPS"));
        assertEquals(20, json.getInt("maxFPS"));
        assertEquals(1.5, json.getDouble("resolution"), 0.0);
        assertEquals(3, json.getInt("maxResolutionLevel"));
    }

    @Test
    public void serializesSlideSyncEventQueuePolicy() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app-id");
        JSONObject defaults = config.toJSON().getJSONObject("slideAppOptions");
        assertFalse(defaults.has("syncEventQueuePolicy"));

        config.getSlideAppOptions().setSyncEventQueuePolicy(SlideSyncEventQueuePolicy.LatestPendingRender);
        JSONObject json = config.toJSON().getJSONObject("slideAppOptions");

        assertEquals("latest-pending-render", json.getString("syncEventQueuePolicy"));
    }

    @Test
    public void serializesLocalLogOptionsUnderLoggerOptions() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app-id");
        LoggerOptions loggerOptions = new LoggerOptions();
        loggerOptions.setLocalLog(new LocalLogOptions()
                .setEnabled(true)
                .setEnabledUpload(false));
        config.setLoggerOptions(loggerOptions);

        JSONObject json = config.toJSON()
                .getJSONObject("loggerOptions")
                .getJSONObject("localLog");

        assertEquals(true, json.getBoolean("enabled"));
        assertEquals(false, json.getBoolean("enabledUpload"));
    }
}
