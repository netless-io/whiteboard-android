package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PptPageTest {
    @Test
    public void serialization() {
        Gson gson = new GsonBuilder().serializeNulls().create();

        String expected = "{\"src\":\"url\",\"width\":400.0,\"height\":400.0}";
        PptPage pptPage = new PptPage("url", 400.0, 400.0);

        assertEquals(expected, gson.toJson(pptPage));
    }

    @Test
    public void deserialization_src() {
        Gson gson = new Gson();

        String json = "{\"src\":\"url\",\"width\":400.0,\"height\":400.0}";
        PptPage pptPage1 = gson.fromJson(json, PptPage.class);
        assertEquals("url", pptPage1.getSrc());

        String alternateJson = "{\"conversionFileUrl\":\"url\",\"width\":400.0,\"height\":400.0}";
        PptPage pptPage2 = gson.fromJson(alternateJson, PptPage.class);
        assertEquals("url", pptPage2.getSrc());
    }

    @Test
    public void deserialization_conversionFileUrl() {
        Gson gson = new Gson();

        String json = "{\"conversionFileUrl\":\"url\",\"width\":400.0,\"height\":400.0}";
        PptPage pptPage = gson.fromJson(json, PptPage.class);
        assertEquals("url", pptPage.getSrc());
    }
}