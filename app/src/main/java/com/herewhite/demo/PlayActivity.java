package com.herewhite.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.herewhite.sdk.*;
import com.herewhite.sdk.domain.*;

public class PlayActivity extends AppCompatActivity {

    private WhiteBroadView whiteBroadView;
    Player player;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Intent intent = getIntent();
        String uuid = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);
        if (uuid != null) {
            whiteBroadView = findViewById(R.id.white);
            player(uuid);
        }
    }

    private void player(String uuid) {
        WhiteSdk whiteSdk = new WhiteSdk(
                whiteBroadView,
                PlayActivity.this,
                new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1, true),
                new UrlInterrupter() {
                    @Override
                    public String urlInterrupter(String sourceUrl) {
                        return sourceUrl;
                    }
                });

        PlayerConfiguration playerConfiguration = new PlayerConfiguration();
//        playerConfiguration.setRoom("f892bd37ba6c4031a8e59b52d308f829");
        playerConfiguration.setRoom(uuid);
        //TODO:提供更正式的 m3u8
//        playerConfiguration.setAudioUrl("https://ohuuyffq2.qnssl.com/98398e2c5a43d74321214984294c157e_60def9bac25e4a378235f6249cae63c1.m3u8");

        whiteSdk.createPlayer(playerConfiguration, new AbstractPlayerEventListener() {
            @Override
            public void onPhaseChanged(PlayerPhase phase) {
                showToast(gson.toJson(phase));
            }

            @Override
            public void onLoadFirstFrame() {
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
                showToast(time);
            }

            @Override
            public void onCatchErrorWhenAppendFrame(SDKError error) {
                showToast(error.getJsStack());
            }

            @Override
            public void onCatchErrorWhenRender(SDKError error) {
                showToast(error.getJsStack());
            }

            @Override
            public void onCursorViewsUpdate(UpdateCursor updateCursor) {
                showToast(gson.toJson(updateCursor));
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
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }
}
