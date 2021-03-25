package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static com.herewhite.sdk.CommonTestTools.compareJson;
import static org.junit.Assert.assertTrue;

public class CameraBoundTest {
    @Test
    public void serialization() {
        Gson gson = new GsonBuilder().serializeNulls().create();

        String expected = "{\"damping\":null,\"centerX\":null,\"centerY\":null,\"width\":null,\"height\":null,\"maxContentMode\":null,\"minContentMode\":null}";
        CameraBound cameraBound = new CameraBound();

        assertTrue(compareJson(expected, gson.toJson(cameraBound)));
    }
}