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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.*;
import com.herewhite.sdk.CombinePlayer.PlayerSyncManager;
import com.herewhite.sdk.domain.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PlayActivity extends AppCompatActivity {

    private WhiteboardView whiteboardView;
    Player player;
    NativePlayerImplement nativePlayer;
    PlayerSyncManager playerSyncManager;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = getIntent();
        final String uuid = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);

        try {
            nativePlayer = new NativePlayerImplement(this, "http://archive.org/download/BigBuckBunny_328/BigBuckBunny_512kb.mp4");
            Log.e("nativePlayer", "create success");
        } catch (Throwable e) {
            Log.e("nativePlayer", "create fail");
        }

        if (uuid != null) {
            whiteboardView = findViewById(R.id.white);
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
        playerSyncManager.play();
    }

    public void pause(MenuItem item) {
        playerSyncManager.pause();
    }

    public void seek(MenuItem item) {
        if (player.getPlayerPhase().equals(PlayerPhase.waitingFirstFrame)) {
            return;
        } else {
            //12秒的视频画面，区别明显；白板画面，看不出来，要看 scheduleTime 变化
            nativePlayer.seek(12, TimeUnit.SECONDS);
        }
    }

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
                Log.i("onScheduleTimeChanged", String.valueOf(time));
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
//                wPlayer.play();
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
                SurfaceView surfaceView = findViewById(R.id.surfaceView);
                nativePlayer.setSurfaceView(surfaceView);
                nativePlayer.setPlayerSyncManager(playerSyncManager);
                playerSyncManager.play();
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
