package com.herewhite.sdk.domain;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RoomPhaseTest {
    private Gson gson = new Gson();

    @Test
    public void testConnecting() {
        RoomPhase phase = RoomPhase.connecting;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("connecting"));
    }

    @Test
    public void testConnected() {
        RoomPhase phase = RoomPhase.connected;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("connected"));
    }


    @Test
    public void testReconnecting() {
        RoomPhase phase = RoomPhase.reconnecting;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("reconnecting"));
    }


    @Test
    public void testDisconnecting() {
        RoomPhase phase = RoomPhase.disconnecting;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("disconnecting"));
    }

    @Test
    public void testDisconnected() {
        RoomPhase phase = RoomPhase.disconnected;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("disconnected"));
    }
}