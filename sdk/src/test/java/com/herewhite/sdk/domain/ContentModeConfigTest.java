package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static com.herewhite.sdk.CommonTestTools.compareJson;
import static org.junit.Assert.assertTrue;

public class ContentModeConfigTest {
    @Test
    public void serialization() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        String expected = "[{\"scale\":1.0,\"space\":0.0,\"mode\":\"Scale\"},{\"scale\":1.0,\"space\":0.0,\"mode\":\"AspectFillScale\"},{\"scale\":1.0,\"space\":0.0,\"mode\":\"Scale\"},{\"scale\":1.0,\"space\":0.0,\"mode\":\"Scale\"},{\"scale\":1.0,\"space\":0.0,\"mode\":\"Scale\"},{\"scale\":1.0,\"space\":0.0,\"mode\":\"Scale\"}]";

        ContentModeConfig center = new ContentModeConfig();
        center.setMode(ContentModeConfig.ScaleMode.CENTER);
        ContentModeConfig centerInside = new ContentModeConfig();
        centerInside.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE);
        ContentModeConfig centerInsideScale = new ContentModeConfig();
        centerInside.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE_SCALE);
        ContentModeConfig centerInsideSpace = new ContentModeConfig();
        centerInside.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE_SPACE);
        ContentModeConfig centerCrop = new ContentModeConfig();
        centerInside.setMode(ContentModeConfig.ScaleMode.CENTER_CROP);

        ContentModeConfig centerCropSpace = new ContentModeConfig();
        centerInside.setMode(ContentModeConfig.ScaleMode.CENTER_CROP_SPACE);

        ContentModeConfig[] configs = new ContentModeConfig[]{
                center, centerInside, centerInsideScale, centerInsideSpace, centerCrop, centerCropSpace
        };

        assertTrue(compareJson(expected, gson.toJson(configs)));
    }
}