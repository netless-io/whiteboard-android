package com.herewhite.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StartActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.whiteSDKDemo.UUID";
    DemoAPI demoAPI = new DemoAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        demoAPI.downloadZip("https://convertcdn.netless.link/dynamicConvert/e1ee27fdb0fc4b7c8f649291010c4882.zip", getCacheDir().getAbsolutePath());
    }

    String getUuid() {
        EditText text = findViewById(R.id.editText);
        return text.getText().toString();
    }

    void tokenAlert() {
        tokenAlert("token", "请在 https://console.herewhite.com 中注册，并获取 sdk token，再进行使用");
    }

    void tokenAlert(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(StartActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL,
                "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public void joinNewRoom(View view) {
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

    public void pureReplay(View view) {
        if (!demoAPI.validateToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, PureReplayActivity.class);

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
