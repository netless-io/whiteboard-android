package com.herewhite.sdk.domain;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WindowParamsTest {
    @Test
    public void serializesUseBoxesStatus() throws Exception {
        WindowParams params = new WindowParams().setUseBoxesStatus(true);

        JSONObject json = params.toJSON();

        assertEquals(true, json.getBoolean("useBoxesStatus"));
    }
}
