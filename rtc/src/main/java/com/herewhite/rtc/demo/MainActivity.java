package com.herewhite.rtc.demo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.jumpToRtc).setOnClickListener(v -> jumpToRtc());
    }

    private void jumpToRtc() {
        Intent intent = new Intent(this, MainRtcActivity.class);
        startActivity(intent);
    }
}