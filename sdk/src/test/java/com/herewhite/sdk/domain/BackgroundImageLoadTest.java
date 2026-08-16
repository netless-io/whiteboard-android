package com.herewhite.sdk.domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.json.JSONObject;
import org.junit.Test;

public class BackgroundImageLoadTest {
    @Test
    public void optionsHaveStableDefaultsAndAcceptBoundaryValues() {
        BackgroundImageLoadOptions options = new BackgroundImageLoadOptions();
        assertEquals(3, options.getMaxRetries());
        assertEquals(15000, options.getTimeoutMs());
        assertEquals(1000, options.getRetryIntervalMs());

        options.setMaxRetries(-1);
        options.setTimeoutMs(1000);
        options.setRetryIntervalMs(0);
        assertEquals(-1, options.getMaxRetries());
        assertEquals(1000, options.getTimeoutMs());
        assertEquals(0, options.getRetryIntervalMs());

        options.setMaxRetries(10);
        options.setTimeoutMs(120000);
        options.setRetryIntervalMs(30000);
        assertEquals(10, options.getMaxRetries());
        assertEquals(120000, options.getTimeoutMs());
        assertEquals(30000, options.getRetryIntervalMs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidRetryCount() {
        new BackgroundImageLoadOptions().setMaxRetries(11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidTimeout() {
        new BackgroundImageLoadOptions().setTimeoutMs(999);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidRetryInterval() {
        new BackgroundImageLoadOptions().setRetryIntervalMs(-1);
    }

    @Test
    public void parsesCancelledEvent() throws Exception {
        JSONObject json = new JSONObject()
                .put("name", "backgroundImageLoad")
                .put("state", "cancelled")
                .put("source", "whiteSdk")
                .put("resourceId", "resource-1")
                .put("resourceUrl", "https://example.com/image.png?token=value#fragment")
                .put("viewId", "mainView")
                .put("scenePath", "/scene-1");

        BackgroundImageLoadEvent event = new BackgroundImageLoadEvent(json);
        assertEquals("backgroundImageLoad", event.name);
        assertEquals("cancelled", event.state);
        assertEquals("whiteSdk", event.source);
        assertEquals("https://example.com/image.png?token=value#fragment", event.resourceUrl);
        assertEquals("mainView", event.viewId);
    }

    @Test
    public void reloadParamsKeepBackgroundImageSource() {
        ReloadBackgroundImageParams params =
                new ReloadBackgroundImageParams("appliance", "app-1", "/scene-1");

        assertEquals("appliance", params.getSource());
        assertEquals("app-1", params.getViewId());
        assertEquals("/scene-1", params.getScenePath());
    }

    @Test
    public void queryParamsKeepViewSceneUrlAndSources() {
        HasBackgroundImageParams params = new HasBackgroundImageParams(
                "mainView",
                "/scene-1",
                "https://example.com/background.webp?token=value#fragment",
                new String[]{"appliance"});

        assertEquals("mainView", params.getViewId());
        assertEquals("/scene-1", params.getScenePath());
        assertEquals(
                "https://example.com/background.webp?token=value#fragment",
                params.getImageUrl());
        assertEquals(Arrays.asList("appliance"), Arrays.asList(params.getSources()));
    }
}
