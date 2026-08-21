package com.herewhite.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.test.window.WindowAppliancePluginActivity;

public class StartActivity extends BaseActivity {

    private static final String ROOM_UUID = "cd1221809b8111f1aaeead87383431cf";
    private static final String ROOM_TOKEN = "NETLESSROOM_YWs9VWtNUk92M1JIN2I2Z284dCZleHBpcmVBdD0xNzg3Mzc4Mzk0OTY3Jm5vbmNlPTg4MzQzNjcwLTlkMjUtMTFmMS1iYzM4LWQ3Yjg5YzgwZTNlMSZyb2xlPTEmc2lnPWZkYjI5MjRkNmMwYTkxMmMzZWQ5YmU5OGZmMTZlNjA3ZDQyYjgxMzljNzA4NTVjMzVkOGM5NzlmOGJjMmY0NTUmdXVpZD1jZDEyMjE4MDliODExMWYxYWFlZWFkODczODM0MzFjZg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setupDemoItems();
    }

    private void setupDemoItems() {
        LinearLayout container = findViewById(R.id.container);

        Button button = getButton(getString(R.string.appliance_plugin));
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, WindowAppliancePluginActivity.class);
            intent.putExtra(SampleBaseActivity.EXTRA_ROOM_UUID, ROOM_UUID);
            intent.putExtra(SampleBaseActivity.EXTRA_ROOM_TOKEN, ROOM_TOKEN);
            startActivity(intent);
        });
        container.addView(button, getLayoutParams());
    }

    private LinearLayout.LayoutParams getLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 16, 16, 16);
        return params;
    }

    private @NonNull Button getButton(String title) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextSize(16);
        button.setAllCaps(false);
        return button;
    }
}
