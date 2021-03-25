package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeviceTypeTest {
    Gson gson = new Gson();

    @Test
    public void testDesktop() {
        DeviceType mode = DeviceType.desktop;
        JsonElement modeElement = gson.toJsonTree(mode);
        String modeString = modeElement.getAsString();
        assertFalse(modeString.equals("Desktop"));
        assertTrue(modeString.equals("desktop"));
    }

    @Test
    public void testTouch() {
        DeviceType mode = DeviceType.touch;
        JsonElement modeElement = gson.toJsonTree(mode);
        String modeString = modeElement.getAsString();
        assertFalse(modeString.equals("Touch"));
        assertTrue(modeString.equals("touch"));
    }
}