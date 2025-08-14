package com.herewhite.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.test.window.WindowAppliancePluginActivity;
import com.herewhite.demo.test.window.WindowTestActivity;

public class StartActivity extends BaseActivity {

    // 数据驱动的按钮配置
    private static class DemoItem {
        String title;
        Class<?> targetClass;
        Runnable specialAction;

        DemoItem(String title, Class<?> targetClass) {
            this.title = title;
            this.targetClass = targetClass;
        }

        DemoItem(String title, Runnable specialAction) {
            this.title = title;
            this.specialAction = specialAction;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setupDemoItems();
    }

    private void setupDemoItems() {
        DemoItem[] items = {
                new DemoItem(getString(R.string.join_room), RoomActivity.class),
                // new DemoItem(getString(R.string.create), RoomActivity.class),
                new DemoItem(getString(R.string.replay), PlayActivity.class),
                new DemoItem(getString(R.string.replay_pure), PureReplayActivity.class),
                new DemoItem(getString(R.string.window_room), WindowTestActivity.class),
                // new DemoItem("Apps", WindowAppsActivity.class),
                new DemoItem(getString(R.string.appliance_plugin), WindowAppliancePluginActivity.class),
                // new DemoItem("NoAppliancePlugin", WindowNoAppliancePluginActivity.class),
                // new DemoItem("混音", this::jumpToRtc)
        };

        LinearLayout container = findViewById(R.id.container);
        for (DemoItem item : items) {
            Button button = getButton(item);
            button.setOnClickListener(v -> {
                if (DemoAPI.get().invalidToken()) {
                    showAlert("token", "请在 https://console.herewhite.com 中注册，并获取 sdk token，再进行使用");
                    return;
                }

                if (item.specialAction != null) {
                    // 特殊处理，如jumpToRtc
                    item.specialAction.run();
                } else {
                    // 普通的Class<?>方式启动
                    Intent intent = new Intent(this, item.targetClass);
                    startActivity(intent);
                }
            });
            container.addView(button, getLayoutParams());
        }
    }

    private LinearLayout.LayoutParams getLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 16, 16, 16);
        return params;
    }

    private @NonNull Button getButton(DemoItem item) {
        Button button = new Button(this);
        button.setText(item.title);
        button.setTextSize(16);
        button.setAllCaps(false);
        return button;
    }

    private void jumpToRtc() {
        try {
            Class<?> clazz = Class.forName("com.herewhite.rtc.demo.MainRtcActivity");
            Intent intent = new Intent(this, clazz);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            showAlert("rtc demo", "config local.properties app.enableRtc=false");
        }
    }
}
