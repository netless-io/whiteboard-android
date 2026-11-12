package com.herewhite.demo.test.window;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.herewhite.sdk.*;
import com.herewhite.sdk.domain.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Debug-only controller for real Native SDK joins and room commands. */
public class ReleaseE2EActivity extends Activity {
    private final Gson gson = new Gson();
    private final Map<String, Object> results = new LinkedHashMap<>();
    private WhiteboardView view;
    private WhiteSdk sdk;
    private Room room;
    private JsonObject credentials;
    private int preloadCallbacks;
    private boolean destroyed;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view = new WhiteboardView(this);
        view.getSettings().setAllowUniversalAccessFromFileURLs(true);
        setContentView(view, new ViewGroup.LayoutParams(-1, -1));
        // Push a short-lived room fixture into the debug app's private files directory.
        // Credentials are never compiled into the APK or copied to SDK assets.
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(new File(getFilesDir(), "release-e2e.json")), StandardCharsets.UTF_8)) {
            credentials = gson.fromJson(reader, JsonObject.class);
            for (String key : new String[]{"appIdentifier", "uuid", "roomToken"}) {
                if (!credentials.has(key) || credentials.get(key).getAsString().isEmpty())
                    throw new IllegalArgumentException("Missing fixture field: " + key);
            }
        } catch (Exception error) {
            android.widget.TextView message = new android.widget.TextView(this);
            message.setText("Install the private release-e2e.json room fixture before running E2E.");
            setContentView(message);
            return;
        }
        long delay = Math.max(0, Math.min(10000, getIntent().getLongExtra("initDelayMs", 0)));
        new Handler(Looper.getMainLooper()).postDelayed(this::initializeSdk, delay);
    }

    private void initializeSdk() {
        if (destroyed) return;
        WhiteSdkConfiguration config = new WhiteSdkConfiguration(credentials.get("appIdentifier").getAsString(), true);
        if (credentials.has("apiHosts")) {
            config.setApiHosts(gson.fromJson(credentials.get("apiHosts"),
                    new com.google.gson.reflect.TypeToken<java.util.List<String>>() {}.getType()));
        }
        config.setRegion(Region.cn);
        config.setUseMultiViews(getIntent().getBooleanExtra("multiViews", true));
        config.setEnableAppliancePlugin(getIntent().getBooleanExtra("enablePlugin", true));
        boolean preloading = getIntent().getBooleanExtra("preload", false);
        config.setEnablePreLoadAppliancePlugin(getIntent().getBooleanExtra("appliancePreload", preloading));
        if (preloading) config.setPreloadBuiltinApps(java.util.Arrays.asList("Slide", "Presentation"));
        if (getIntent().getBooleanExtra("enableModelPreload", false)) config.setEnableModelPreload(true);
        if (getIntent().hasExtra("dynamicConfig")) {
            java.util.Map<String, String> urls = gson.fromJson(getIntent().getStringExtra("dynamicConfig"),
                    new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>() {}.getType());
            config.setDynamicConfig(urls);
        }
        if ("empty".equals(getIntent().getStringExtra("apps")))
            config.setPreloadBuiltinApps(Collections.emptyList());
        String workerMode = getIntent().getStringExtra("workerMode");
        if (workerMode != null) {
            AppliancePluginOptions options = new AppliancePluginOptions();
            Map<String, Object> extras = new LinkedHashMap<>();
            extras.put("useWorker", "mainThread".equals(workerMode) ? "mainThread" : "auto");
            extras.put("useSimple", !"dual".equals(workerMode));
            options.setExtras(extras); config.setAppliancePluginOptions(options);
        }
        // Optional flags are set before constructing the SDK, just like host code.
        if (getIntent().hasExtra("sharedMediaVolume"))
            config.getSlideAppOptions().setEnableSharedMediaVolume(getIntent().getBooleanExtra("sharedMediaVolume", false));
        config.getSlideAppOptions().setEnableScale(true);
        config.getSlideAppOptions().setEnableGlobalClick(false);
        config.getSlideAppOptions().setSyncEventQueuePolicy(SlideSyncEventQueuePolicy.LatestPendingRender);
        sdk = new WhiteSdk(view, this, config, new CommonCallback() {
            @Override public void onPreloadStateChange(WhitePreloadResult result) {
                preloadCallbacks++;
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("state", result.getState()); payload.put("message", result.getMessage());
                payload.put("count", preloadCallbacks);
                publish("preload", payload);
            }
            @Override public void sdkSetupFail(SDKError error) { publishError("setupFailure", error); }
        });
        Map<String, Object> configuration = new LinkedHashMap<>();
        configuration.put("apps", "empty".equals(getIntent().getStringExtra("apps"))
                ? Collections.emptyList() : config.getPreloadBuiltinApps());
        configuration.put("sharedMediaVolume", config.getSlideAppOptions().isEnableSharedMediaVolume());
        configuration.put("enableModelPreload", config.isEnableModelPreload());
        configuration.put("appliance", !config.isEnableAppliancePlugin() ? "disabled"
                : config.isEnablePreLoadAppliancePlugin() ? "constructor" : "post-room");
        configuration.put("sdk", config);
        configuration.put("nativeVersion", WhiteSdk.Version());
        publish("configuration", configuration);
        if (getIntent().getBooleanExtra("immediateJoin", false)) {
            // Exercise the original host flow: construct, then join immediately,
            // without calling prepareCompletion or waiting for preload callbacks.
            Intent request = new Intent(getIntent());
            request.putExtra("action", "join");
            request.putExtra("requestId", "immediateJoin");
            request.putExtra("uid", "android-immediate-" + SystemClock.elapsedRealtime());
            onNewIntent(request);
            return;
        }
        sdk.prepareCompletion(new Promise<Void>() {
            @Override public void then(Void value) { publish("prepared", "ready"); }
            @Override public void catchEx(SDKError error) { publishError("prepared", error); }
        });
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String action = intent.getStringExtra("action");
        String id = intent.getStringExtra("requestId");
        try {
            if (sdk == null) throw new IllegalStateException("Runtime room fixture is unavailable");
            if ("join".equals(action)) {
                RoomParams params = new RoomParams(credentials.get("uuid").getAsString(),
                        credentials.get("roomToken").getAsString(), intent.getStringExtra("uid"));
                params.setRegion(Region.cn);
                params.setWritable(true);
                params.setDisableCameraTransform(false);
                params.setUndoCacheScenesCount(32);
                params.setWindowParams(new WindowParams().setForceMaximized(true)
                        .setLazySetupInMaximizedMode(true).setMaxCachedAppsInMaximizedMode(4)
                        .setOriginSize(new WindowOriginSize(1280, 720)));
                Map<String, Object> start = new LinkedHashMap<>();
                start.put("event", "joinStart"); start.put("elapsedMs", SystemClock.elapsedRealtime());
                start.put("uid", intent.getStringExtra("uid"));
                publish(id + "-start", start);
                sdk.joinRoom(params, new AbstractRoomCallbacks() {
                    @Override public void onPhaseChanged(RoomPhase phase) {
                        publish("phase-" + SystemClock.elapsedRealtime(), phase.name());
                    }
                }, new Promise<Room>() {
                    @Override public void then(Room value) {
                        room = value;
                        publish(id, "connected");
                    }
                    @Override public void catchEx(SDKError error) { publishError(id, error); }
                });
            } else if ("leave".equals(action)) {
                sdk.leaveRoom(new Promise<Object>() {
                    @Override public void then(Object value) { room = null; publish(id, "closed"); }
                    @Override public void catchEx(SDKError error) { publishError(id, error); }
                });
            } else if ("pencil".equals(action)) {
                MemberState state = gson.fromJson(intent.getStringExtra("payload"), MemberState.class);
                room.setMemberState(state);
                publish(id, "configured");
            } else if ("enableUndo".equals(action)) {
                // Single-board rooms keep the legacy disableSerialization=true default.
                // Opt in explicitly before testing the public undo/redo APIs.
                room.disableSerialization(false);
                publish(id, "enabled");
            } else if ("docs".equals(action)) {
                WindowDocsEvent event = gson.fromJson(intent.getStringExtra("payload"), WindowDocsEvent.class);
                room.dispatchDocsEvent(event, new Promise<DispatchDocsEventResult>() {
                    @Override public void then(DispatchDocsEventResult value) { publish(id, value); }
                    @Override public void catchEx(SDKError error) { publishError(id, error); }
                });
            } else if ("docsSequence".equals(action)) {
                // Schedule on the device: ADB round-trip time must not set the page cadence.
                JsonArray sequence = gson.fromJson(intent.getStringExtra("payload"), JsonArray.class);
                Handler handler = new Handler(Looper.getMainLooper());
                long start = SystemClock.uptimeMillis() + 100;
                for (int i = 0; i < sequence.size(); i++) {
                    final int index = i;
                    final WindowDocsEvent event = gson.fromJson(sequence.get(i), WindowDocsEvent.class);
                    handler.postAtTime(() -> {
                        publish(id + "-start-" + index, event);
                        room.dispatchDocsEvent(event, new Promise<DispatchDocsEventResult>() {
                            @Override public void then(DispatchDocsEventResult value) {
                                publish(id + "-result-" + index, value);
                            }
                            @Override public void catchEx(SDKError error) {
                                publishError(id + "-result-" + index, error);
                            }
                        });
                    }, start + i * 100L);
                }
                handler.postAtTime(() -> publish(id, "sent"), start + sequence.size() * 100L);
            } else if ("addSlide".equals(action)) {
                WindowAppParam app = WindowAppParam.createSlideApp(
                        "9340e8e067bc11ec8f582b1b98453394",
                        "https://convertcdn.netless.link/dynamicConvert", "Release E2E fixture");
                app.setOriginSize(new WindowOriginSize(1280, 720));
                room.addApp(app, new Promise<String>() {
                    @Override public void then(String value) { publish(id, value); }
                    @Override public void catchEx(SDKError error) { publishError(id, error); }
                });
            } else { publish(id, "unknown action"); }
        } catch (Exception error) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("error", error.getClass().getSimpleName() + ": " + error.getMessage());
            publish(id, value);
        }
    }

    private void publishError(String id, SDKError error) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("error", error.getMessage()); publish(id, value);
    }

    private void publish(String id, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("elapsedMs", SystemClock.elapsedRealtime()); result.put("value", value);
        results.put(id, result);
        view.evaluateJavascript("window.__nativeE2E=" + gson.toJson(results), null);
    }

    @Override public void onDestroy() {
        destroyed = true;
        if (sdk != null) sdk.releaseRoom();
        if (view != null) view.destroy();
        super.onDestroy();
    }
}
