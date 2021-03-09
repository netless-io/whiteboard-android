package com.herewhite.demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.herewhite.demo.exo.WhiteExoPlayer;
import com.herewhite.demo.ijk.WhiteIjkPlayer;
import com.herewhite.demo.ijk.widget.media.IjkVideoView;
import com.herewhite.sdk.Player;
import com.herewhite.sdk.PlayerEventListener;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.combinePlayer.NativePlayer;
import com.herewhite.sdk.combinePlayer.PlayerSyncManager;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;

public class PlayActivity extends BaseActivity implements PlayerEventListener {
    private final String TAG = "player";
    private final String TAG_Native = "nativePlayer";

    Gson gson = new Gson();
    private DemoAPI demoAPI = new DemoAPI();

    protected WhiteboardView mWhiteboardView;
    protected SeekBar mSeekBar;
    /*
     * 如果不需要音视频混合播放，可以直接操作 Player
     */
    @Nullable
    PlayerSyncManager mPlayerSyncManager;
    @Nullable
    Player mPlaybackPlayer;
    @Nullable
    NativePlayer mWhiteMediaPlayer;

    private boolean mUserIsSeeking;
    // 是否使用 ExoPlayer，true 使用 EXOPlayer，false 则使用 IjkPlayer，默认为 true
    private boolean mIsUsedExoPlayer = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mSeekBar = findViewById(R.id.player_seek_bar);
        mWhiteboardView = findViewById(R.id.white);
        WebView.setWebContentsDebuggingEnabled(true);

        testMarkIdling(false);
        setupPlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mIsUsedExoPlayer && Util.SDK_INT > 23 && mWhiteMediaPlayer != null) {
            ((WhiteExoPlayer) mWhiteMediaPlayer).onResume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsUsedExoPlayer && Util.SDK_INT <= 23 && mWhiteMediaPlayer != null) {
            ((WhiteExoPlayer) mWhiteMediaPlayer).onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsUsedExoPlayer && Util.SDK_INT <= 23 && mWhiteMediaPlayer != null) {
            ((WhiteExoPlayer) mWhiteMediaPlayer).onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsUsedExoPlayer && Util.SDK_INT > 23 && mWhiteMediaPlayer != null) {
            ((WhiteExoPlayer) mWhiteMediaPlayer).onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerSyncManager != null) {
            mPlayerSyncManager.pause();
        }
        if (mWhiteMediaPlayer != null) {
            if (mIsUsedExoPlayer) {
                ((WhiteExoPlayer) mWhiteMediaPlayer).release();
            } else {
                ((WhiteIjkPlayer) mWhiteMediaPlayer).release();
            }
            mWhiteMediaPlayer = null;
        }
        mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
    }

    //region private
    private boolean isPlayable() {
        return mPlayerSyncManager != null && mPlaybackPlayer != null && mWhiteMediaPlayer != null;
    }

    public void play(android.view.View button) {
        play();
    }

    public void pause(android.view.View button) {
        pause();
    }

    public void reset(android.view.View button) {
        seek(0L);
    }

    //region override
    protected void setupPlayer() {
        final String uuid = getIntent().getStringExtra(StartActivity.EXTRA_MESSAGE);

        try {
            if (mIsUsedExoPlayer) {
                // WhiteExoPlayer demo
                PlayerView playerView = findViewById(R.id.exo_video_view);
                playerView.setVisibility(View.VISIBLE);
                mWhiteMediaPlayer = new WhiteExoPlayer(this);
                ((WhiteExoPlayer) mWhiteMediaPlayer).setPlayerView(playerView);
                ((WhiteExoPlayer) mWhiteMediaPlayer).setVideoPath("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4");
            } else {
                // WhiteIjkPlayer demo
                IjkVideoView videoView = findViewById(R.id.ijk_video_view);
                videoView.setVisibility(View.VISIBLE);
                mWhiteMediaPlayer = new WhiteIjkPlayer(videoView);
                ((WhiteIjkPlayer) mWhiteMediaPlayer).setVideoPath("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4");
            }

            mPlayerSyncManager = new PlayerSyncManager(mWhiteMediaPlayer, new PlayerSyncManager.Callbacks() {
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

    void enableBtn() {
        findViewById(R.id.button_play).setEnabled(true);
        findViewById(R.id.button_pause).setEnabled(true);
        findViewById(R.id.button_reset).setEnabled(true);
    }

    //region menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.replayer_command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void orientation(MenuItem item) {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            PlayActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            PlayActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public void getTimeInfo(MenuItem item) {
        Log.i(TAG, gson.toJson(mPlaybackPlayer.getPlayerTimeInfo()));
    }

    public void getPlayState(MenuItem item) {
        Log.i(TAG, gson.toJson(mPlaybackPlayer.getPlayerState()));
    }

    public void getPhase(MenuItem item) {
        Log.i(TAG, gson.toJson(mPlaybackPlayer.getPlayerPhase()));
    }

    //endregion

    protected void play() {
        if (isPlayable()) {
            mPlayerSyncManager.play();
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 100);
        }
    }

    protected void pause() {
        if (isPlayable()) {
            mPlayerSyncManager.pause();
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
        }
    }

    protected void seek(Long time, TimeUnit timeUnit) {
        if (isPlayable()) {
            // nativePlayer 会调用 PlayerSync
            if (mIsUsedExoPlayer) {
                ((WhiteExoPlayer) mWhiteMediaPlayer).seek(time, timeUnit);
            } else {
                ((WhiteIjkPlayer) mWhiteMediaPlayer).seek(time, timeUnit);
            }
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 100);
        }
    }

    protected void seek(float progress) {
        if (isPlayable()) {
            PlayerTimeInfo timeInfo = mPlaybackPlayer.getPlayerTimeInfo();
            long time = (long) (progress * timeInfo.getTimeDuration());
            seek(time, TimeUnit.MILLISECONDS);
            mSeekBar.setProgress((int) playerProgress());
        }
    }

    //region seekBar
    protected Handler mSeekBarUpdateHandler = new Handler();
    protected Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mUserIsSeeking) {
                return;
            }
            // FIXME:正在 seek 时，progress 会被重置到旧的时间，只有 seek 完成，progress 才会恢复正确
            float progress = playerProgress();
            Log.v(TAG, "progress: " + progress);
            mSeekBar.setProgress((int) progress);
            mSeekBarUpdateHandler.postDelayed(this, 100);
        }
    };

    protected void setupSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userSelectedPosition = 0;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    userSelectedPosition = progress;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = false;
                seek(userSelectedPosition / 100f);
            }
        });
    }

    //endregion

    @Override
    public void onPhaseChanged(PlayerPhase phase) {
        mPlayerSyncManager.updateWhitePlayerPhase(phase);
    }

    @Override
    public void onLoadFirstFrame() {
        Log.i(TAG, "onLoadFirstFrame: ");
        showToast("onLoadFirstFrame");
    }

    @Override
    public void onSliceChanged(String slice) {
        //一般不需要实现
    }

    @Override
    public void onPlayerStateChanged(PlayerState modifyState) {
        Log.i(TAG, "onPlayerStateChanged: " + gson.toJson(modifyState));
    }

    @Override
    public void onStoppedWithError(SDKError error) {
        Log.d(TAG, "onStoppedWithError: " + error.getJsStack());
        showToast(error.getJsStack());
    }

    @Override
    public void onScheduleTimeChanged(long time) {
        Log.v(TAG, "onScheduleTimeChanged: " + time);
    }

    @Override
    public void onCatchErrorWhenAppendFrame(SDKError error) {
        showToast(error.getJsStack());
    }

    @Override
    public void onCatchErrorWhenRender(SDKError error) {
        showToast(error.getJsStack());
    }
    //endregion

    protected void initPlayer(String uuid, String roomToken) {
        WhiteSdk whiteSdk = new WhiteSdk(
                mWhiteboardView,
                PlayActivity.this,
                new WhiteSdkConfiguration(demoAPI.getAppId(), true),
                new UrlInterrupter() {
                    @Override
                    public String urlInterrupter(String sourceUrl) {
                        return sourceUrl;
                    }
                });

        PlayerConfiguration playerConfiguration = new PlayerConfiguration(uuid, roomToken);
        // 只回放 60 秒。如果时间太长，seek bar 进度条移动不明显。
        // 实际播放时，需要对齐原始音视频和白板
        playerConfiguration.setDuration(60000L);

        whiteSdk.createPlayer(playerConfiguration, this, new Promise<Player>() {
            @Override
            public void then(Player wPlayer) {
                mPlaybackPlayer = wPlayer;
                mPlayerSyncManager.setWhitePlayer(mPlaybackPlayer);

                setupSeekBar();
                if (mWhiteMediaPlayer != null) {
                    if (mIsUsedExoPlayer) {
                        ((WhiteExoPlayer) mWhiteMediaPlayer).setPlayerSyncManager(mPlayerSyncManager);
                    } else {
                        ((WhiteIjkPlayer) mWhiteMediaPlayer).setPlayerSyncManager(mPlayerSyncManager);
                    }
                }
                // seek 一次才能主动触发
                mPlaybackPlayer.seekToScheduleTime(0);
                enableBtn();
                play();
                mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 100);

                testMarkIdling(true);
            }

            @Override
            public void catchEx(SDKError t) {
                alert("create player error, ", t.getJsStack());
            }
        });
    }
    //endregion

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }

    public void alert(final String title, final String detail) {
        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(PlayActivity.this).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(detail);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                alertDialog.show();
            }
        });
    }

    float playerProgress() {
        if (mPlaybackPlayer == null || mPlaybackPlayer.getPlayerPhase() == PlayerPhase.waitingFirstFrame) {
            return 0;
        }
        PlayerTimeInfo timeInfo = mPlaybackPlayer.getPlayerTimeInfo();
        return (float) timeInfo.getScheduleTime() / timeInfo.getTimeDuration() * 100.f;
    }
}
