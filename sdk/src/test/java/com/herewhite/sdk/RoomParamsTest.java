package com.herewhite.sdk;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RoomParamsTest {
    @Test
    public void serializesUndoCacheScenesCountOnlyWhenConfigured() throws Exception {
        RoomParams params = new RoomParams("room-uuid", "room-token", "uid");

        assertFalse(params.toJSON().has("undoCacheScenesCount"));

        params.setUndoCacheScenesCount(32);
        JSONObject json = params.toJSON();

        assertEquals(Integer.valueOf(32), params.getUndoCacheScenesCount());
        assertEquals(32, json.getInt("undoCacheScenesCount"));
    }
}
