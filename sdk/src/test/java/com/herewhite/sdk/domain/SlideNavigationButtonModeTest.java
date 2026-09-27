package com.herewhite.sdk.domain;

import com.herewhite.sdk.WhiteSdkConfiguration;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class SlideNavigationButtonModeTest {
    @Test
    public void omitsUnsetModeToPreserveAppSlideDefault() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app-id");
        assertNull(config.getSlideAppOptions().getNavigationButtonMode());
        assertFalse(config.toJSON().getJSONObject("slideAppOptions").has("navigationButtonMode"));
    }

    @Test
    public void serializesPageAndStepUsingWebOptionValues() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app-id");
        WhiteSdkConfiguration.SlideAppOptions options = new WhiteSdkConfiguration.SlideAppOptions();
        config.setSlideAppOptions(options);

        options.setNavigationButtonMode(SlideNavigationButtonMode.Page);
        assertEquals(SlideNavigationButtonMode.Page, options.getNavigationButtonMode());
        JSONObject page = config.toJSON().getJSONObject("slideAppOptions");
        assertEquals("page", page.getString("navigationButtonMode"));

        options.setNavigationButtonMode(SlideNavigationButtonMode.Step);
        assertEquals(SlideNavigationButtonMode.Step, options.getNavigationButtonMode());
        JSONObject step = config.toJSON().getJSONObject("slideAppOptions");
        assertEquals("step", step.getString("navigationButtonMode"));
    }

    @Test
    public void clearingModeRestoresDefaultOmission() throws Exception {
        WhiteSdkConfiguration config = new WhiteSdkConfiguration("app-id");
        config.getSlideAppOptions().setNavigationButtonMode(SlideNavigationButtonMode.Step);
        config.getSlideAppOptions().setNavigationButtonMode(null);

        assertNull(config.getSlideAppOptions().getNavigationButtonMode());
        assertFalse(config.toJSON().getJSONObject("slideAppOptions").has("navigationButtonMode"));
    }
}
