package com.herewhite.sdk.domain;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class RoomStateTest {
    @Test
    public void roomStateConverting() {
        String s = "{\"roomMembers\":[{\"memberId\":9,\"memberState\":{\"currentApplianceName\":" +
                "\"pencil\",\"strokeColor\":[104,171,93],\"strokeWidth\":4,\"textSize\":16}," +
                "\"session\":\"40c7b3605fe24c7c8c654ef421fa16fc\"},{\"memberId\":10," +
                "\"memberState\":{\"currentApplianceName\":\"pencil\",\"strokeColor\":" +
                "[0,91,246],\"strokeWidth\":4,\"textSize\":16},\"session\":" +
                "\"4f6f7a787e3b4abab3f3ea2cce8968a3\",\"payload\":{\"id\":\"122\"}}]}";
        RoomState state = new Gson().fromJson(s, RoomState.class);
        Map<String, String> payload = (Map<String, String>) state.getRoomMembers()[1].getPayload();
        assertTrue(payload.get("id").equals("122"));
    }
}
