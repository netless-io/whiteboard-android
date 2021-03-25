package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ViewModeTest {
    private Gson gson = new Gson();

    @Test
    public void testBroadcaster() {
        ViewMode mode = ViewMode.Broadcaster;
        JsonElement modeElement = gson.toJsonTree(mode);
        String modeString = modeElement.getAsString();
        assertFalse(modeString.equals("Broadcaster"));
        assertTrue(modeString.equals("broadcaster"));
    }

    @Test
    public void testFollower() {
        ViewMode mode = ViewMode.Follower;
        JsonElement modeElement = gson.toJsonTree(mode);
        String modeString = modeElement.getAsString();
        assertFalse(modeString.equals("Follower"));
        assertTrue(modeString.equals("follower"));
    }

    @Test
    public void testFreedom() {
        ViewMode mode = ViewMode.Freedom;
        JsonElement modeElement = gson.toJsonTree(mode);
        String modeString = modeElement.getAsString();
        assertFalse(modeString.equals("Freedom"));
        assertTrue(modeString.equals("freedom"));
    }
}