package com.herewhite.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.herewhite.sdk.Utils.PreFetcher;

public class StartActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.whiteSDKDemo.UUID";
    DemoAPI demoAPI = new DemoAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreFetcher prefetcher = new PreFetcher();
        prefetcher.setResultCallback(new PreFetcher.ResultCallback() {
            @Override
            public void fetchOriginConfigFail(Exception exception) {

            }

            @Override
            public void fetchOriginConfigSuccess(JsonObject jsonObject) {

            }

            @Override
            public void finishPrefetch(JsonObject jsonObject) {
                Log.i("fetch", "finishPrefetch: " + jsonObject.toString());
            }
        });
        prefetcher.fetchOriginConfigs();
    }

    public String getUuid() {
        EditText text = findViewById(R.id.editText);
        return text.getText().toString();
    }

    public void tokenAlert() {
        tokenAlert("token", "请在 https://console.herewhite.com 中注册，并获取 sdk token，再进行使用");
    }

    public void tokenAlert(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(StartActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void createRoom(View view) {
        if (!demoAPI.validateToken()) {
            tokenAlert();
            return;
        }
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
    }

    public void joinRoom(View view) {
        if (!demoAPI.validateToken()) {
            tokenAlert();
            return;
        }
        Intent intent = new Intent(this, RoomActivity.class);

        String uuid = getUuid();
        if (uuid.length() > 0) {
            intent.putExtra(EXTRA_MESSAGE, uuid);
        }
        startActivity(intent);
    }

    public void replayRoom(View view) {
        if (!demoAPI.validateToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, PlayActivity.class);
        
        String uuid = getUuid();
        if (uuid.length() > 0) {
            intent.putExtra(EXTRA_MESSAGE, uuid);
            startActivity(intent);
        } else if (demoAPI.getDemoUUID().length() > 0) {
            intent.putExtra(EXTRA_MESSAGE, demoAPI.getDemoUUID());
            startActivity(intent);
        } else {
            tokenAlert("uuid", "请填入回放用 uuid");
        }
    }
}
