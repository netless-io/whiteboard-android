package com.herewhite.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.test.CameraBoundScenesActivity;
import com.herewhite.demo.test.CameraMoveActivity;
import com.herewhite.demo.test.CameraMoveImageActivity;
import com.herewhite.demo.test.ConvertV5Activity;
import com.herewhite.demo.test.HTErrorActivity;
import com.herewhite.demo.test.PptOldActivity;
import com.herewhite.demo.test.PptResourceCacheActivity;
import com.herewhite.demo.test.RatioChangeActivity;
import com.herewhite.demo.test.ScenesActivity;
import com.herewhite.demo.test.SyncedStoreActivity;
import com.herewhite.demo.test.WebSocketTestActivity;
import com.herewhite.demo.test.window.WindowAppliancePluginActivity;
import com.herewhite.demo.test.window.WindowAppsActivity;
import com.herewhite.demo.test.window.WindowFullscreenActivity;
import com.herewhite.demo.test.window.WindowHVChangeActivity;
import com.herewhite.demo.test.window.WindowNoAppliancePluginActivity;
import com.herewhite.demo.test.window.WindowPageTestActivity;
import com.herewhite.demo.test.window.WindowRegisterAppActivity;
import com.herewhite.demo.test.window.WindowRestoreActivity;

public class QaActivity extends BaseActivity {
    private static class DemoItem {
        final String title;
        final Class<?> targetClass;

        DemoItem(String title, Class<?> targetClass) {
            this.title = title;
            this.targetClass = targetClass;
        }
    }

    private static class DemoGroup {
        final String title;
        final DemoItem[] items;

        DemoGroup(String title, DemoItem[] items) {
            this.title = title;
            this.items = items;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setTitle(R.string.qa_debug_examples);
        setupDemoItems();
    }

    private void setupDemoItems() {
        DemoGroup[] groups = {
                new DemoGroup(getString(R.string.qa_single_window_group), new DemoItem[]{
                        new DemoItem(getString(R.string.qa_scene_tests), ScenesActivity.class),
                        new DemoItem(getString(R.string.qa_synced_store), SyncedStoreActivity.class),
                        new DemoItem(getString(R.string.qa_camera_move), CameraMoveActivity.class),
                        new DemoItem(getString(R.string.qa_camera_move_image), CameraMoveImageActivity.class),
                        new DemoItem(getString(R.string.qa_camera_bound_scenes), CameraBoundScenesActivity.class),
                        new DemoItem(getString(R.string.qa_ratio_change), RatioChangeActivity.class),
                        new DemoItem(getString(R.string.qa_ht_error), HTErrorActivity.class),
                }),
                new DemoGroup(getString(R.string.qa_window_group), new DemoItem[]{
                        new DemoItem(getString(R.string.qa_window_apps), WindowAppsActivity.class),
                        new DemoItem(getString(R.string.qa_window_fullscreen), WindowFullscreenActivity.class),
                        new DemoItem(getString(R.string.qa_window_restore), WindowRestoreActivity.class),
                        new DemoItem(getString(R.string.qa_window_register_app), WindowRegisterAppActivity.class),
                        new DemoItem(getString(R.string.qa_window_page), WindowPageTestActivity.class),
                        new DemoItem(getString(R.string.qa_window_orientation), WindowHVChangeActivity.class),
                        new DemoItem(getString(R.string.qa_window_appliance_plugin), WindowAppliancePluginActivity.class),
                        new DemoItem(getString(R.string.qa_window_no_appliance_plugin), WindowNoAppliancePluginActivity.class),
                }),
                new DemoGroup(getString(R.string.qa_resource_group), new DemoItem[]{
                        new DemoItem(getString(R.string.qa_convert_v5), ConvertV5Activity.class),
                        new DemoItem(getString(R.string.qa_web_socket), WebSocketTestActivity.class),
                        new DemoItem(getString(R.string.qa_ppt_old), PptOldActivity.class),
                        new DemoItem(getString(R.string.qa_ppt_resource_cache), PptResourceCacheActivity.class),
                }),
        };

        LinearLayout container = findViewById(R.id.container);
        for (DemoGroup group : groups) {
            container.addView(getHeader(group.title), getLayoutParams());
            for (DemoItem item : group.items) {
                Button button = getButton(item);
                button.setOnClickListener(v -> openDemo(item));
                container.addView(button, getLayoutParams());
            }
        }
    }

    private void openDemo(DemoItem item) {
        if (DemoAPI.get().invalidToken()) {
            showAlert("token", "请在 https://console.herewhite.com 中注册，并获取 sdk token，再进行使用");
            return;
        }

        startActivity(new Intent(this, item.targetClass));
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

    private @NonNull TextView getHeader(String title) {
        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setTextSize(20);
        return textView;
    }
}
