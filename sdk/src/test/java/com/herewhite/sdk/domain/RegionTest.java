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
    private String testSG = "{\"region\":\"sg\"}";
    private String testIN = "{\"region\":\"in-mum\"}";
    private String testGB = "{\"region\":\"gb-lon\"}";

    @Test
    public void serialize() {
        Foobar foobar = new Foobar();

        foobar.setRegion(Region.cn);
        assertEquals(testCN, new Gson().toJson(foobar));

        foobar.setRegion(Region.us);
        assertEquals(testUS, new Gson().toJson(foobar));

        foobar.setRegion(Region.sg);
        assertEquals(testSG, new Gson().toJson(foobar));

        foobar.setRegion(Region.in_mum);
        assertEquals(testIN, new Gson().toJson(foobar));

        foobar.setRegion(Region.gb_lon);
        assertEquals(testGB, new Gson().toJson(foobar));
    }

    @Test
    public void deserialize() {
        assertEquals(Region.cn, new Gson().fromJson(testCN, Foobar.class).region);

        assertEquals(Region.us, new Gson().fromJson(testUS, Foobar.class).region);

        assertEquals(Region.sg, new Gson().fromJson(testSG, Foobar.class).region);

        assertEquals(Region.in_mum, new Gson().fromJson(testIN, Foobar.class).region);

        assertEquals(Region.gb_lon, new Gson().fromJson(testGB, Foobar.class).region);
    }
}