package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnimationModeTest {

    private Gson gson = new Gson();

    @Test
    public void testContinuous() {
        AnimationMode mode = AnimationMode.Continuous;
        JsonElement modeElement = gson.toJsonTree(mode);
        String modeString = modeElement.getAsString();
        assertFalse(modeString.equals("Continuous"));
        assertTrue(modeString.equals("continuous"));
    }

    @Test
    public void testImmediately() {
        AnimationMode mode = AnimationMode.Immediately;
        JsonElement modeElement = gson.toJsonTree(mode);
        String modeString = modeElement.getAsString();
        assertFalse(modeString.equals("Immediately"));
        assertTrue(modeString.equals("immediately"));
    }
}