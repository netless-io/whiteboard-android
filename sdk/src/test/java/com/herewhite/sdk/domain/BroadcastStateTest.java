package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static com.herewhite.sdk.CommonTestTools.compareJson;
import static org.junit.Assert.assertTrue;

public class BroadcastStateTest {
    @Test
    public void serialization() {
        Gson gson = new GsonBuilder().serializeNulls().create();

        String expected = "{\"mode\":null,\"broadcasterId\":null,\"broadcasterInformation\":null}";
        BroadcastState broadcastState = new BroadcastState();

        assertTrue(compareJson(expected, gson.toJson(broadcastState)));
    }
}