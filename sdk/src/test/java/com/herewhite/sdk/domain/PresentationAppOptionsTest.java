package com.herewhite.sdk.domain;

import com.herewhite.sdk.WhiteSdkConfiguration;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
