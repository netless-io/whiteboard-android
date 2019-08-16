package com.herewhite.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.*;
import com.herewhite.sdk.domain.*;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PlayActivity extends AppCompatActivity {

    private WhiteboardView whiteboardView;
    Player player;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = getIntent();
        final String uuid = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);
        if (uuid != null) {
            whiteboardView = findViewById(R.id.white);

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

    public void seek(MenuItem item) {
        if (player.getPlayerPhase().equals(PlayerPhase.waitingFirstFrame)) {
            return;
        } else {
            player.seekToScheduleTime(3 * 1000);
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
        //TODO:提供更正式的 m3u8
//        playerConfiguration.setAudioUrl("https://ohuuyffq2.qnssl.com/98398e2c5a43d74321214984294c157e_60def9bac25e4a378235f6249cae63c1.m3u8");

        whiteSdk.createPlayer(playerConfiguration, new AbstractPlayerEventListener() {
            @Override
            public void onPhaseChanged(PlayerPhase phase) {
                Log.i("player info", "onPhaseChanged: " + phase);
                showToast(gson.toJson(phase));
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
                wPlayer.play();
                player = wPlayer;
            }

            @Override
            public void catchEx(SDKError t) {
                Logger.error("create player error, ", t);
            }
        });
    }

    void showToast(Object o) {
        Log.i("showToast", o.toString());
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }
}
