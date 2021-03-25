package com.herewhite.sdk.domain;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegionTest {

    private class Foobar {
        private Region region;

        public Region getRegion() {
            return region;
        }

        public void setRegion(Region region) {
            this.region = region;
        }
    }

    private String testCN = "{\"region\":\"cn-hz\"}";
    private String testUS = "{\"region\":\"us-sv\"}";

    @Test
    public void serialize() {
        Foobar foobar = new Foobar();

        foobar.setRegion(Region.cn);
        assertEquals(testCN, new Gson().toJson(foobar));

        foobar.setRegion(Region.us);
        assertEquals(testUS, new Gson().toJson(foobar));


        new Gson().toJson(Region.cn);
    }

    @Test
    public void deserialize() {
        assertEquals(Region.cn, new Gson().fromJson(testCN, Foobar.class).region);

        assertEquals(Region.us, new Gson().fromJson(testUS, Foobar.class).region);
    }
}