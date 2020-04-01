package com.herewhite.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.Toast;
import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;

import com.google.gson.Gson;
import com.herewhite.sdk.AbstractPlayerEventListener;
import com.herewhite.sdk.combinePlayer.PlayerSyncManager;
import com.herewhite.sdk.Logger;
import com.herewhite.sdk.Player;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PlayActivity extends PureReplayActivity {

    @Nullable
    NativeMediaPlayer nativePlayer;
    /*
     * 如果不需要音视频混合播放，可以直接操作 Player
     */
    @Nullable
    PlayerSyncManager playerSyncManager;
    private final String TAG_Native = "nativePlayer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mSeekBar = findViewById(R.id.player_seek_bar);
    }

    //region private
    private boolean isPlayable() {
        return playerSyncManager != null && player != null && nativePlayer != null;
    }

    public void play(android.view.View button) {
        play();
    }

    public void pause(android.view.View button) {
        pause();
    }

    public void reset(android.view.View button) {
        seek(0l);
    }
    //endregion

    //region override
    @Override
    protected void setupPlayer() {
        Intent intent = getIntent();
        final String uuid = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);

        try {
            nativePlayer = new NativeMediaPlayer(this, "http://archive.org/download/BigBuckBunny_328/BigBuckBunny_512kb.mp4");
            playerSyncManager = new PlayerSyncManager(nativePlayer, new PlayerSyncManager.Callbacks() {
                @Override
                public void startBuffering() {
                    Log.d(TAG_Native, "startBuffering: ");
                }

                @Override
                public void endBuffering() {
                    Log.d(TAG_Native, "endBuffering: ");

                }
            });
            Log.d(TAG_Native, "create success");
        } catch (Throwable e) {
            Log.e(TAG_Native, "create fail");
        }

        DemoAPI demoAPI = new DemoAPI();

        DemoAPI.Result result = new DemoAPI.Result() {
            @Override
            public void success(String uuid, String roomToken) {
                initPlayer(uuid, roomToken);
            }

            @Override
            public void fail(String message) {
                alert("创建回放失败: ", message);
            }
        };

        if (uuid != null) {
            demoAPI.getRoomToken(uuid, result);
        } else if (demoAPI.hasDemoInfo()) {
            demoAPI.getNewRoom(result);
        } else {
            alert("无数据", "没有房间 uuid");
        }
    }

    @Override
    protected void play() {
        if (isPlayable()) {
            playerSyncManager.play();
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 100);
        }
    }

    @Override
    protected void pause() {
        if (isPlayable()) {
            playerSyncManager.pause();
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
        }
    }

    @Override
    protected void seek(Long time, TimeUnit timeUnit) {
        if (isPlayable()) {
            // nativePlayer 会调用 PlayerSync
            nativePlayer.seek(time, timeUnit);
        }
    }

    @Override
    protected void seek(float progress) {
        if (isPlayable()) {
            PlayerTimeInfo timeInfo = player.getPlayerTimeInfo();
            long time = (long) (progress * timeInfo.getTimeDuration());
            seek(time, TimeUnit.MILLISECONDS);
            mSeekBar.setProgress((int) playerProgress());
        }
    }

    @Override
    protected void initPlayer(String uuid, String roomToken) {
        WhiteSdk whiteSdk = new WhiteSdk(
                whiteboardView,
                PlayActivity.this,
                new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1, true),
                new UrlInterrupter() {
                    @Override
                    public String urlInterrupter(String sourceUrl) {
                        return sourceUrl;
                    }
                });

        PlayerConfiguration playerConfiguration = new PlayerConfiguration(uuid, roomToken);
        // 只回放 60 秒。如果时间太长，seek bar 进度条移动不明显。
        // 实际播放时，需要对齐原始音视频和白板
        playerConfiguration.setDuration(60000l);

        whiteSdk.createPlayer(playerConfiguration, this, new Promise<Player>() {
            @Override
            public void then(Player wPlayer) {
                player = wPlayer;
                setupSeekBar();
                SurfaceView surfaceView = findViewById(R.id.surfaceView);
                playerSyncManager.setWhitePlayer(player);
                nativePlayer.setSurfaceView(surfaceView);
                nativePlayer.setPlayerSyncManager(playerSyncManager);
                // seek 一次才能主动触发
                wPlayer.seekToScheduleTime(0);
                enableBtn();
                play();
                mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 100);
            }

            @Override
            public void catchEx(SDKError t) {
                alert("create player error, ", t.getJsStack());
            }
        });
    }
    //endregion
}
