package com.herewhite.sdk;

import com.herewhite.sdk.domain.PlayerObserverMode;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wendu.dsbridge.OnReturnValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PlayerTest {
    private static final double DOUBLE_DELTA = 0.000001f;

    JsBridgeInterface mockJsBridgeInterface;
    private Player mPlayer;

    @Before
    public void setUp() throws Exception {
        mockJsBridgeInterface = mock(JsBridgeInterface.class);
        mPlayer = new Player("7e7c8f007a4011eba97639a3a8d1dde1", mockJsBridgeInterface, 3);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setAndGetPlaybackSpeed() {
        mPlayer.setPlaybackSpeed(2.0f);
        verify(mockJsBridgeInterface).callHandler(eq("player.setPlaybackSpeed"), (Object[]) any());
        assertEquals(2.0f, mPlayer.getPlaybackSpeed(), DOUBLE_DELTA);

        mPlayer.getPlaybackSpeed(new Promise<Double>() {
            @Override
            public void then(Double aDouble) {

            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
        verify(mockJsBridgeInterface).callHandler(eq("player.state.playbackSpeed"), (OnReturnValue<Object>) any());
    }

    @Test
    public void play() {
        mPlayer.play();
        verify(mockJsBridgeInterface).callHandler(eq("player.play"), (Object[]) any());
    }

    @Test
    public void pause() {
        mPlayer.pause();
        verify(mockJsBridgeInterface).callHandler(eq("player.pause"), (Object[]) any());
    }

    @Test
    public void stop() {
        mPlayer.stop();
        verify(mockJsBridgeInterface).callHandler(eq("player.stop"), (Object[]) any());
    }

    @Test
    public void seekToScheduleTime() {
        mPlayer.seekToScheduleTime(100L);
        verify(mockJsBridgeInterface).callHandler(eq("player.seekToScheduleTime"), aryEq(new Object[]{100L}));
    }

    @Test
    public void setObserverMode() {
        mPlayer.setObserverMode(PlayerObserverMode.directory);
        verify(mockJsBridgeInterface).callHandler(eq("player.setObserverMode"), aryEq(new Object[]{"directory"}));

        mPlayer.setObserverMode(PlayerObserverMode.freedom);
        verify(mockJsBridgeInterface).callHandler(eq("player.setObserverMode"), aryEq(new Object[]{"freedom"}));
    }

    @Test
    public void getPlayerPhase() {
    }

    @Test
    public void getPhase() {
        mPlayer.getPhase(new Promise<PlayerPhase>() {
            @Override
            public void then(PlayerPhase playerPhase) {

            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
        verify(mockJsBridgeInterface).callHandler(eq("player.getBroadcastState"), any(), any());
    }

    @Test
    public void getPlayerState() {
        PlayerState playerPhase = mPlayer.getPlayerState();
        assertNull(playerPhase);

        mPlayer.getPlayerState(new Promise<PlayerState>() {
            @Override
            public void then(PlayerState playerState) {

            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
        verify(mockJsBridgeInterface).callHandler(eq("player.state.playerState"), any(), any());
    }

    @Test
    public void getPlayerTimeInfo() {
        PlayerTimeInfo playerTimeInfo = mPlayer.getPlayerTimeInfo();
        assertEquals(0, playerTimeInfo.getScheduleTime());

        mPlayer.getPlayerTimeInfo(new Promise<PlayerTimeInfo>() {
            @Override
            public void then(PlayerTimeInfo playerTimeInfo) {

            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
        verify(mockJsBridgeInterface).callHandler(eq("player.state.timeInfo"), any(), any());
    }
}