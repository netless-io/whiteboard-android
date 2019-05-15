package com.herewhite.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class StartActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.whiteSDKDemo.UUID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public String getUuid() {
        EditText text = findViewById(R.id.editText);
        return text.getText().toString();
    }

    public void createRoom(View view) {
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
    }

    public void joinRoom(View view) {
        Intent intent = new Intent(this, RoomActivity.class);
        if (getUuid().length() > 0) {
            intent.putExtra(EXTRA_MESSAGE, getUuid());
        }
        startActivity(intent);
    }

    public void replayRoom(View view) {
        Intent intent = new Intent(this, RoomActivity.class);
        if (getUuid().length() > 0) {
            intent.putExtra(EXTRA_MESSAGE, getUuid());
            startActivity(intent);
        } else {
            Log.w("error", "none uuid");
        }
    }
}
