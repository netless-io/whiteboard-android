package com.herewhite.demo.test.window;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.DispatchDocsEventResult;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.SlideSyncEventQueuePolicy;
import com.herewhite.sdk.domain.UnifiedPageState;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowDocsEvent;
import com.herewhite.sdk.domain.WindowOriginSize;
import com.herewhite.sdk.domain.WindowPageStateOptions;
import com.herewhite.sdk.domain.WindowParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Debug-only Native -> Bridge -> WindowManager navigation regression controller. */
public final class SlideNavigationRegressionActivity extends Activity {
    private static final String TAG = "WB_SLIDE_NAV";
    private final Gson gson = new Gson();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WhiteboardView view;
    private WhiteSdk sdk;
    private Room room;
    private String appId;
    private boolean destroyed;
    private int runGeneration;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view = new WhiteboardView(this);
        view.getSettings().setAllowUniversalAccessFromFileURLs(true);
        setContentView(view, new ViewGroup.LayoutParams(-1, -1));
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(
                new File(getFilesDir(), "slide-navigation.json")), StandardCharsets.UTF_8)) {
            JsonObject fixture = gson.fromJson(reader, JsonObject.class);
            WhiteSdkConfiguration config = new WhiteSdkConfiguration(required(fixture, "appIdentifier"), true);
            config.setRegion(Region.cn);
            config.setUseMultiViews(true);
            config.setEnableAppliancePlugin(true);
            config.getSlideAppOptions().setEnableGlobalClick(false);
            config.getSlideAppOptions().setEnableScale(true);
            config.getSlideAppOptions().setSyncEventQueuePolicy(SlideSyncEventQueuePolicy.Fifo);
            sdk = new WhiteSdk(view, this, config);
            RoomParams params = new RoomParams(required(fixture, "uuid"), required(fixture, "roomToken"),
                    "android-slide-nav-" + System.currentTimeMillis());
            params.setRegion(Region.cn);
            params.setWritable(true);
            params.setWindowParams(new WindowParams().setForceMaximized(true)
                    .setLazySetupInMaximizedMode(true).setMaxCachedAppsInMaximizedMode(4)
                    .setOriginSize(new WindowOriginSize(1280, 720)));
            sdk.joinRoom(params, new AbstractRoomCallbacks() {}, new Promise<Room>() {
                @Override public void then(Room value) {
                    if (destroyed) return;
                    room = value;
                    publish("joined", value.getWritable());
                    WindowAppParam app = WindowAppParam.createSlideApp(
                            "edbec11b18ad496592384d87efd62c4f",
                            "https://dev-common-classroom.educdn.cn/shengwang/test_live_classroom/dynamicConvert",
                            "Infinite animation navigation regression");
                    room.addApp(app, new Promise<String>() {
                        @Override public void then(String id) {
                            if (destroyed) return;
                            appId = id;
                            dispatch("initial-jump", WindowDocsEvent.JumpToPage(13).withTarget(appId));
                            awaitPage13(SystemClock.elapsedRealtime() + 90000);
                        }
                        @Override public void catchEx(SDKError error) { publishError("add", error); }
                    });
                }
                @Override public void catchEx(SDKError error) { publishError("join", error); }
            });
        } catch (Exception error) {
            publish("fixture-error", error.getClass().getSimpleName());
        }
    }

    private static String required(JsonObject fixture, String key) {
        String value = fixture.has(key) ? fixture.get(key).getAsString() : "";
        if (value.isEmpty()) throw new IllegalArgumentException("Missing fixture field: " + key);
        return value;
    }

    private void awaitPage13(long deadline) {
        if (destroyed) return;
        room.getPageState(new WindowPageStateOptions().withTarget(appId), new Promise<UnifiedPageState>() {
            @Override public void then(UnifiedPageState state) {
                if (destroyed) return;
                if (state.getPage() == 13) {
                    Map<String, Object> ready = new LinkedHashMap<>();
                    ready.put("appId", appId); ready.put("writable", room.getWritable());
                    ready.put("page", state.getPage()); publish("ready", ready);
                    if (getIntent().getBooleanExtra("autoRun", true))
                        handler.postDelayed(() -> runCommands("automatic-next", "nextStep", 1, 100), 1000);
                } else if (SystemClock.elapsedRealtime() < deadline) {
                    handler.postDelayed(() -> awaitPage13(deadline), 200);
                } else publish("ready-error", "page-13-timeout");
            }
            @Override public void catchEx(SDKError error) {
                if (SystemClock.elapsedRealtime() < deadline)
                    handler.postDelayed(() -> awaitPage13(deadline), 200);
                else publishError("ready-error", error);
            }
        });
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String id = intent.getStringExtra("requestId");
        if (id == null) id = "request-" + System.currentTimeMillis();
        if (room == null || appId == null || destroyed) { publish(id, "not-ready"); return; }
        if ("run".equals(intent.getStringExtra("action"))) {
            runCommands(id, intent.hasExtra("event") ? intent.getStringExtra("event") : "nextStep",
                    intent.getIntExtra("count", 1), intent.getIntExtra("intervalMs", 100));
        } else if ("docs".equals(intent.getStringExtra("action"))) {
            try {
                WindowDocsEvent event = gson.fromJson(intent.getStringExtra("payload"), WindowDocsEvent.class);
                dispatch(id, event.withTarget(appId));
            } catch (Exception error) { publish(id, "invalid-docs-payload"); }
        } else if ("state".equals(intent.getStringExtra("action"))) {
            final String requestId = id;
            room.getPageState(new WindowPageStateOptions().withTarget(appId), new Promise<UnifiedPageState>() {
                @Override public void then(UnifiedPageState state) { publish(requestId, state); }
                @Override public void catchEx(SDKError error) { publishError(requestId, error); }
            });
        } else publish(id, "unknown-action");
    }

    private void runCommands(String id, String event, int count, int intervalMs) {
        if (!Boolean.TRUE.equals(room.getWritable())) { publish(id, "not-writable"); return; }
        if (!("nextStep".equals(event) || "prevStep".equals(event))
                || count < 1 || count > 500 || intervalMs < 0 || intervalMs > 5000) {
            publish(id, "invalid-run-options"); return;
        }
        final int generation = ++runGeneration;
        final long start = SystemClock.uptimeMillis() + 100;
        publish(id + "-begin", count);
        for (int i = 0; i < count; i++) {
            final int index = i;
            handler.postAtTime(() -> {
                if (destroyed || generation != runGeneration) return;
                publish(id + "-input-" + index, event);
                dispatch(id + "-result-" + index, new WindowDocsEvent(event).withTarget(appId));
            }, start + i * (long) intervalMs);
        }
        handler.postAtTime(() -> {
            if (!destroyed && generation == runGeneration) publish(id + "-input-stopped", count);
        }, start + count * (long) intervalMs);
    }

    private void dispatch(String id, WindowDocsEvent event) {
        room.dispatchDocsEvent(event, new Promise<DispatchDocsEventResult>() {
            @Override public void then(DispatchDocsEventResult result) { publish(id, result); }
            @Override public void catchEx(SDKError error) { publishError(id, error); }
        });
    }

    private void publishError(String id, SDKError error) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("error", error.getMessage()); publish(id, result);
    }

    private void publish(String id, Object value) {
        if (destroyed) return;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("wallMs", System.currentTimeMillis());
        result.put("elapsedMs", SystemClock.elapsedRealtime()); result.put("value", value);
        Log.i(TAG, id + " " + gson.toJson(result));
        view.evaluateJavascript("window.__slideNavigationNative=window.__slideNavigationNative||{};"
                + "window.__slideNavigationNative[" + gson.toJson(id) + "]=" + gson.toJson(result), null);
    }

    @Override public void onDestroy() {
        destroyed = true; runGeneration++; handler.removeCallbacksAndMessages(null);
        if (sdk != null) sdk.releaseRoom();
        if (view != null) view.destroy();
        super.onDestroy();
    }
}
