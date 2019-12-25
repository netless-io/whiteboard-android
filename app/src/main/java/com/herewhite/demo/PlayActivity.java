package com.herewhite.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.gson.JsonObject;
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
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PlayActivity extends AppCompatActivity {

    private WhiteboardView whiteboardView;
    Player player;
    NativeMediaPlayer nativePlayer;
    /**
     * 如果有不需要音视频混合播放，可以直接操作 Player
     */
    PlayerSyncManager playerSyncManager;
    Gson gson = new Gson();
    private static HttpDnsService httpdns;
    private boolean mUserIsSeeking = false;
    private SeekBar mSeekBar;

    private Handler mSeekBarUpdateHandler = new Handler();
    private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (!nativePlayer.isNormalState() || mUserIsSeeking) {
                return;
            }
            Float progress = Float.valueOf(nativePlayer.getCurrentPosition()) / nativePlayer.getDuration() * 100;
            Log.i("nativePlayer", "progress: " + progress );
            mSeekBar.setProgress(progress.intValue());
            mSeekBarUpdateHandler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mSeekBar = findViewById(R.id.player_seek_bar);

        Intent intent = getIntent();
        final String uuid = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);

        try {
            nativePlayer = new NativeMediaPlayer(this, "http://archive.org/download/BigBuckBunny_328/BigBuckBunny_512kb.mp4");
            Log.e("nativePlayer", "create success");
        } catch (Throwable e) {
            Log.e("nativePlayer", "create fail");
        }

        if (uuid != null) {
            whiteboardView = findViewById(R.id.white);
// 阿里云 httpdns 替换
            httpdns = HttpDns.getService(getApplicationContext(), "188301");
            httpdns.setPreResolveHosts(new ArrayList<>(Arrays.asList("expresscloudharestoragev2.herewhite.com", "cloudharev2.herewhite.com", "scdncloudharestoragev3.herewhite.com", "cloudcapiv4.herewhite.com")));
            whiteboardView.setWebViewClient(new WhiteWebviewClient(httpdns));
            WebView.setWebContentsDebuggingEnabled(true);

            new DemoAPI().getRoomToken(uuid, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.code() == 200) {
                            JsonObject room = gson.fromJson(response.body().string(), JsonObject.class);
                            String roomToken = room.getAsJsonObject("msg").get("roomToken").getAsString();
                            player(uuid, roomToken);
                        } else {
                            alert("获取房间 token 失败", response.body().string());
                        }
                    } catch (Throwable e) {
                        alert("获取房间 token 失败", e.toString());
                    }
                }
            });
        }
    }

    //region Menu Item
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.replayer_command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    public void getTimeInfo(MenuItem item) {
        Log.i("getTimeInfo", gson.toJson(player.getPlayerTimeInfo()));
    }

    public void getPlayState(MenuItem item) {
        Log.i("getPlayState", gson.toJson(player.getPlayerState()));
    }

    public void getPhase(MenuItem item) {
        Log.i("getPhase", gson.toJson(player.getPlayerPhase()));
    }


    public void play(MenuItem item) {
        if (playerSyncManager != null) {
            playerSyncManager.play();
        }
    }

    public void pause(MenuItem item) {
        if (playerSyncManager != null) {
            playerSyncManager.pause();
        }
    }

    public void seek(MenuItem item) {
        if (player.getPlayerPhase().equals(PlayerPhase.waitingFirstFrame)) {
            return;
        } else {
            //12秒的视频画面，区别明显；白板画面，看不出来，要看 scheduleTime 变化
            nativePlayer.seek(12, TimeUnit.SECONDS);
        }
    }

    //endregion

    //region private
    public void play() {
        if (playerSyncManager != null) {
            playerSyncManager.play();
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
            mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 100);
        }
    }

    public void pause() {
        if (playerSyncManager != null) {
            playerSyncManager.pause();
            mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
        }
    }

    //region action

    public void play(android.view.View button) {
        play();
    }

    public void pause(android.view.View button) {
        pause();
    }

    public void rest(android.view.View button) {
        if (nativePlayer != null) {
            nativePlayer.seek(0, TimeUnit.SECONDS);
        }
    }

    private void setupSeekBar() {
        SeekBar seekBar = findViewById(R.id.player_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                if (nativePlayer != null && nativePlayer.isNormalState()) {
                    Float progress = userSelectedPosition / 100.f * nativePlayer.getDuration();
                    nativePlayer.seek(progress.intValue(), TimeUnit.SECONDS);
                }
            }
        });
    }

    //endregion

    public void alert(final String title, final String detail) {

        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(PlayActivity.this).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(detail);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    private void player(String uuid, String roomToken) {
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

        whiteSdk.createPlayer(playerConfiguration, new AbstractPlayerEventListener() {
            @Override
            public void onPhaseChanged(PlayerPhase phase) {
                Log.i("player info", "onPhaseChanged: " + phase);
                showToast(gson.toJson(phase));
                if (playerSyncManager != null) {
                    playerSyncManager.updateWhitePlayerPhase(phase);
                }
            }

            @Override
            public void onLoadFirstFrame() {
                Log.i("onLoadFirstFrame", "onLoadFirstFrame: ");
                showToast("onLoadFirstFrame");
            }

            @Override
            public void onSliceChanged(String slice) {
                showToast(slice);
            }

            @Override
            public void onPlayerStateChanged(PlayerState modifyState) {
                showToast(gson.toJson(modifyState));
            }

            @Override
            public void onStoppedWithError(SDKError error) {
                showToast(error.getJsStack());
            }

            @Override
            public void onScheduleTimeChanged(long time) {
//                Log.i("onScheduleTimeChanged", String.valueOf(time));
            }

            @Override
            public void onCatchErrorWhenAppendFrame(SDKError error) {
                showToast(error.getJsStack());
            }

            @Override
            public void onCatchErrorWhenRender(SDKError error) {
                showToast(error.getJsStack());
            }
        }, new Promise<Player>() {
            @Override
            public void then(Player wPlayer) {
                player = wPlayer;
                playerSyncManager = new PlayerSyncManager(player, nativePlayer, new PlayerSyncManager.Callbacks() {
                    @Override
                    public void startBuffering() {
                        showToast("startBuffering");
                    }

                    @Override
                    public void endBuffering() {
                        showToast("endBuffering");
                    }
                });
                setupSeekBar();
                SurfaceView surfaceView = findViewById(R.id.surfaceView);
                nativePlayer.setSurfaceView(surfaceView);
                nativePlayer.setPlayerSyncManager(playerSyncManager);
                playerSyncManager.play();
                mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            }

            @Override
            public void catchEx(SDKError t) {
                Logger.error("create player error, ", t);
            }
        });
    }

    public void orientation(MenuItem item) {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            PlayActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            PlayActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        logRoomInfo( "width:" + whiteboardView.getWidth() / getResources().getDisplayMetrics().density + " height: " + whiteboardView.getHeight() / getResources().getDisplayMetrics().density);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                whiteboardView.evaluateJavascript("player.refreshViewSize()");
//                logRoomInfo( "width:" + whiteboardView.getWidth() / getResources().getDisplayMetrics().density + " height: " + whiteboardView.getHeight() / getResources().getDisplayMetrics().density);
            }
        }, 1000);
    }

    void showToast(Object o) {
        Log.i("showToast", o.toString());
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }
}
