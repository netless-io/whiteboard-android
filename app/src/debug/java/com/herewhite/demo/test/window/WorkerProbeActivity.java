package com.herewhite.demo.test.window;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.herewhite.demo.R;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.AppliancePluginOptions;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WindowParams;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Debug-only, read-only Native join/leave entry for local Worker source validation. */
public class WorkerProbeActivity extends Activity {
    private final List<String> events = new ArrayList<>();
    private final Gson gson = new Gson();
    private WhiteboardView board;
    private WhiteSdk sdk;
    private TextView status;
    private String mode;
    private String roomOverrideMode;
    private boolean joined;
    private boolean busy;
    private boolean destroyed;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String requested = getIntent().getStringExtra("workerMode");
        mode = "dual".equals(requested) ? "dual"
                : ("mainThread".equals(requested) ? "mainThread" : "simple");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout controls = new LinearLayout(this);
        status = new TextView(this);
        status.setText("Worker: " + mode + " / initialized");
        controls.addView(status);
        Button join = new Button(this);
        join.setText("加入（只读）");
        join.setOnClickListener(v -> joinRoom());
        controls.addView(join);
        Button leave = new Button(this);
        leave.setText("退出");
        leave.setOnClickListener(v -> leaveRoom());
        controls.addView(leave);
        layout.addView(controls);
        board = new WhiteboardView(this); // Keep the SDK's local file: entry URL.
        layout.addView(board, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(layout);
        WhiteSdkConfiguration config = new WhiteSdkConfiguration(getString(R.string.sdk_app_id), true);
        config.setRegion(Region.cn);
        config.setUseMultiViews(true);
        config.setEnableAppliancePlugin(true);
        if (!getIntent().getBooleanExtra("remoteOnly", false)) config.setAppliancePluginOptions(optionsFor(mode));
        String dynamicUrl = getIntent().getStringExtra("dynamicConfigUrl");
        if (dynamicUrl != null) { Map<String, String> urls = new LinkedHashMap<>(); urls.put("cn-hz", dynamicUrl); config.setDynamicConfig(urls); }
        sdk = new WhiteSdk(board, this, config);
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Preserve credentials from the launch intent when sending an action only.
        String action = intent.getStringExtra("workerAction");
        if (intent.hasExtra("workerOverrideMode")) {
            String override = intent.getStringExtra("workerOverrideMode");
            roomOverrideMode = "sdk".equals(override) ? null : override;
        }
        if ("join".equals(action)) joinRoom();
        if ("leave".equals(action)) leaveRoom();
    }

    private String roomConfig(String name, int fallback) {
        String value = getIntent().getStringExtra(name);
        return value == null ? getString(fallback) : value;
    }

    private void joinRoom() {
        if (busy || joined || destroyed) return;
        String uuid = roomConfig("roomUuid", R.string.room_uuid);
        String token = roomConfig("roomToken", R.string.room_token);
        if (uuid.isEmpty() || token.isEmpty()) { record("configuration_error"); return; }
        RoomParams params = new RoomParams(uuid, token, "android-worker-" + System.currentTimeMillis());
        params.setRegion(Region.cn);
        params.setWritable(false);
        params.setWindowParams(new WindowParams());
        if (roomOverrideMode != null) params.setAppliancePluginOptions(optionsFor(roomOverrideMode));
        busy = true;
        record("joining");
        sdk.joinRoom(params, new Promise<Room>() {
            @Override public void then(Room room) {
                if (destroyed) return;
                busy = false;
                joined = true;
                record("ready");
            }
            @Override public void catchEx(SDKError error) {
                if (destroyed) return;
                busy = false;
                record("join_error");
            }
        });
    }

    private AppliancePluginOptions optionsFor(String workerMode) {
        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("useWorker", "mainThread".equals(workerMode) ? "mainThread" : "auto");
        extras.put("useSimple", !"dual".equals(workerMode));
        extras.put("allowImageBitmapFallback", true);
        AppliancePluginOptions options = new AppliancePluginOptions(); options.setExtras(extras);
        return options;
    }

    private void leaveRoom() {
        if (busy || !joined || destroyed) return;
        busy = true;
        sdk.leaveRoom(new Promise<Object>() {
            @Override public void then(Object value) {
                if (destroyed) return;
                busy = false;
                joined = false;
                record("left");
            }
            @Override public void catchEx(SDKError error) {
                if (destroyed) return;
                busy = false;
                record("leave_error");
            }
        });
    }

    private void record(String event) {
        events.add(event);
        runOnUiThread(() -> {
            if (destroyed) return;
            status.setText("Worker: " + mode + " / " + event);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("mode", mode);
            result.put("events", events);
            board.evaluateJavascript("window.__androidWorkerProbe=" + gson.toJson(result), null);
            Log.i("WhiteWorkerProbe", "mode=" + mode + " event=" + event);
        });
    }

    @Override public void onDestroy() {
        destroyed = true;
        if (sdk != null) sdk.releaseRoom();
        if (board != null) board.destroy();
        super.onDestroy();
    }
}
