package com.herewhite.sdk.domain;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WhiteScenePathTypeTest {
    private Gson gson = new Gson();

    @Test
    public void testEmpty() {
        WhiteScenePathType type = WhiteScenePathType.Empty;
        String phaseStr = gson.toJsonTree(type).getAsString();
        assertTrue(phaseStr.equals("none"));
    }

    @Test
    public void testPage() {
        WhiteScenePathType type = WhiteScenePathType.Page;
        String phaseStr = gson.toJsonTree(type).getAsString();
        assertTrue(phaseStr.equals("page"));
    }

    @Test
    public void testDir() {
        WhiteScenePathType type = WhiteScenePathType.Dir;
        String phaseStr = gson.toJsonTree(type).getAsString();
        assertTrue(phaseStr.equals("dir"));
    }
}