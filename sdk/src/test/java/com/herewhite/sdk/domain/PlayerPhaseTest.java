package com.herewhite.sdk.domain;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PlayerPhaseTest {
    private Gson gson = new Gson();

    @Test
    public void testWaitingFirstFrame() {
        PlayerPhase phase = PlayerPhase.waitingFirstFrame;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("waitingFirstFrame"));
    }

    @Test
    public void testPlaying() {
        PlayerPhase phase = PlayerPhase.playing;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("playing"));
    }

    @Test
    public void testStop() {
        PlayerPhase phase = PlayerPhase.stopped;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("stop"));
    }

    @Test
    public void testEnded() {
        PlayerPhase phase = PlayerPhase.ended;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("ended"));
    }

    @Test
    public void testBuffering() {
        PlayerPhase phase = PlayerPhase.buffering;
        String phaseStr = gson.toJsonTree(phase).getAsString();
        assertTrue(phaseStr.equals("buffering"));
    }
}