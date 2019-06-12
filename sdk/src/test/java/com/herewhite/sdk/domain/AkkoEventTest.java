package com.herewhite.sdk.domain;
import android.util.Log;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AkkoEventTest {

    AkkoEvent event;

    @Before
    public void initEvent() {
        JSONObject payload = new JSONObject();

        try {
            payload.put("k", "v");
        } catch (Exception e) {
            Log.e("testing error", e.toString());
        }

        event = new AkkoEvent("n", "p");
    }

    @Test
    public void serializer() {
        Gson g = new Gson();
        AkkoEvent tEvent = g.fromJson(g.toJson(event), event.getClass());
        assertTrue(tEvent.getEventName().equals(event.getEventName()));
        assertTrue(tEvent.getPayload().equals(event.getPayload()));
    }
}