package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static com.herewhite.sdk.CommonTestTools.compareJson;
import static org.junit.Assert.assertTrue;

public class FontFaceTest {
    @Test
    public void serialization() {
        Gson gson = new GsonBuilder().serializeNulls().create();

        String expected = "{\"font-family\":\"fontFamily\",\"src\":\"https://online/ttf/patch\",\"font-style\":null,\"font-weight\":null,\"unicode-range\":null}";
        FontFace fontFace = new FontFace("fontFamily", "https://online/ttf/patch");

        assertTrue(compareJson(expected, gson.toJson(fontFace)));
    }
}