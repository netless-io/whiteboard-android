package com.herewhite.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.test.window.WindowAppliancePluginActivity;
import com.herewhite.demo.test.window.WindowAppsActivity;
import com.herewhite.demo.test.window.WindowNoAppliancePluginActivity;
import com.herewhite.demo.test.window.WindowTestActivity;

public class StartActivity extends AppCompatActivity {
    public static final String EXTRA_ROOM_UUID = "com.herewhite.demo.UUID";

    DemoAPI demoAPI = DemoAPI.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    String getUuid() {
        return "";
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
        if (demoAPI.invalidToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
    }

    public void joinRoom(View view) {
        if (demoAPI.invalidToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, RoomActivity.class);
        String uuid = getUuid();
        if (uuid.length() > 0) {
            intent.putExtra(EXTRA_ROOM_UUID, uuid);
        }
        startActivity(intent);
    }

    public void replayRoom(View view) {
        if (demoAPI.invalidToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, PlayActivity.class);
        String uuid = getUuid();
        if (uuid.length() > 0) {
            intent.putExtra(EXTRA_ROOM_UUID, uuid);
            startActivity(intent);
        } else if (demoAPI.getRoomUUID().length() > 0) {
            intent.putExtra(EXTRA_ROOM_UUID, demoAPI.getRoomUUID());
            startActivity(intent);
        } else {
            tokenAlert("uuid", "请填入回放用 uuid");
        }
    }

    public void pureReplay(View view) {
        if (demoAPI.invalidToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, PureReplayActivity.class);

        String uuid = getUuid();
        if (uuid.length() > 0) {
            intent.putExtra(EXTRA_ROOM_UUID, uuid);
            startActivity(intent);
        } else if (demoAPI.getRoomUUID().length() > 0) {
            intent.putExtra(EXTRA_ROOM_UUID, demoAPI.getRoomUUID());
            startActivity(intent);
        } else {
            tokenAlert("uuid", "请填入回放用 uuid");
        }
    }

    public void windowTest(View view) {
        if (demoAPI.invalidToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, WindowTestActivity.class);
        startActivity(intent);
    }

    public void windowAppsTest(View view) {
        if (demoAPI.invalidToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, WindowAppsActivity.class);
        startActivity(intent);
    }

    public void appliancePlugin(View view) {
        if (demoAPI.invalidToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, WindowAppliancePluginActivity.class);
        startActivity(intent);
    }

    public void noAppliancePlugin(View view) {
        if (demoAPI.invalidToken()) {
            tokenAlert();
            return;
        }

        Intent intent = new Intent(this, WindowNoAppliancePluginActivity.class);
        startActivity(intent);
    }
}
