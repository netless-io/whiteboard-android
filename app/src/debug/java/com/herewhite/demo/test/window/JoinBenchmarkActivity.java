package com.herewhite.demo.test.window;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;

import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.SlideSyncEventQueuePolicy;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowOriginSize;
import com.herewhite.sdk.domain.WindowParams;

import org.json.JSONObject;

import java.util.UUID;
import wendu.dsbridge.special.OnReturnValue;

/** Debug-only cold process benchmark, with optional room-free local preload. */
public final class JoinBenchmarkActivity extends Activity {
    private static final String TAG = "WB_INIT_BENCH";
    private String runId;
    private BenchmarkView view;
    private WhiteSdk sdk;
    private Room room;
    private volatile boolean destroyed;
    private boolean joinStarted;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        runId = getIntent().getStringExtra("runId");
        if (runId == null || runId.isEmpty()) runId = UUID.randomUUID().toString();
        try {
            Intent intent = getIntent();
            String appId = required(intent, "appId");
            String uuid = required(intent, "roomUuid");
            String token = required(intent, "roomToken");
            String uid = required(intent, "uid");
            final boolean preload = intent.getBooleanExtra("preload", false);
            if (preload) emit("PRELOAD_START", null, null);
            view = new BenchmarkView(this);
            view.addJavascriptInterface(new SourceProbe(), "__wbInitBenchNative");
            view.getSettings().setAllowUniversalAccessFromFileURLs(true);
            setContentView(view, new ViewGroup.LayoutParams(-1, -1));

            WhiteSdkConfiguration config = new WhiteSdkConfiguration(appId, true);
            config.setRegion(Region.cn);
            config.setUseMultiViews(true);
            config.setEnableAppliancePlugin(true);
            config.setPreloadDynamicPPT(false);
            config.getSlideAppOptions().setEnableScale(true);
            config.getSlideAppOptions().setEnableGlobalClick(false);
            config.getSlideAppOptions().setSyncEventQueuePolicy(SlideSyncEventQueuePolicy.LatestPendingRender);
            RoomParams params = new RoomParams(uuid, token, uid);
            params.setRegion(Region.cn);
            params.setWritable(true);
            params.setDisableCameraTransform(false);
            params.setUndoCacheScenesCount(32);
            params.setWindowParams(new WindowParams().setForceMaximized(true)
                    .setLazySetupInMaximizedMode(true).setMaxCachedAppsInMaximizedMode(4).setUseBoxesStatus(false)
                    .setOriginSize(new WindowOriginSize(1280, 720)));

            if (preload) {
                view.prepared = () -> runOnUiThread(() -> {
                    if (destroyed || joinStarted) return;
                    emit("PRELOAD_READY", null, null);
                    beginJoin(params);
                });
                view.failed = () -> runOnUiThread(() -> emit("ERROR", "local_sdk_preparation_failed", null));
                view.postDelayed(() -> { if (!destroyed && !joinStarted) emit("ERROR", "local_sdk_preparation_timeout", null); }, 30000);
            }
            view.startEntry();
            sdk = new WhiteSdk(view, this, config);
            if (!preload) beginJoin(params);
        } catch (Exception error) {
            // Config errors contain field/asset names only; never serialize an Intent or SDKError.
            emit("ERROR", "init_failed_" + error.getClass().getSimpleName(), null);
            finish();
        }
    }

    private void beginJoin(RoomParams params) {
            if (destroyed || joinStarted) return;
            joinStarted = true;
            emit("T0", null, null);
            sdk.joinRoom(params, new AbstractRoomCallbacks() {}, new Promise<Room>() {
                @Override public void then(Room value) {
                    if (destroyed) return;
                    room = value;
                    emit("NATIVE_JOIN_SUCCESS", null, null);
                }
                @Override public void catchEx(SDKError error) { emit("ERROR", "join_failed", null); }
            });
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String requestId = intent.getStringExtra("requestId");
        String action = intent.getStringExtra("action");
        if ("leave".equals(action)) {
            if (room == null || destroyed) {
                emit("ERROR", "room_not_connected", requestId);
                return;
            }
            final Room leaving = room;
            try {
                leaving.disconnect(new Promise<Object>() {
                    @Override public void then(Object value) {
                        if (room == leaving) room = null;
                        emit("LEFT", null, requestId);
                    }
                    @Override public void catchEx(SDKError error) { emit("ERROR", "leave_failed", requestId); }
                });
            } catch (Exception error) {
                emit("ERROR", "leave_failed", requestId);
            }
            return;
        }
        if (!"addSlide".equals(action)) {
            emit("ERROR", "unsupported_action", requestId);
            return;
        }
        if (room == null || destroyed) {
            emit("ERROR", "room_not_connected", requestId);
            return;
        }
        try {
            JSONObject payload = new JSONObject(required(intent, "payload"));
            String taskId = payload.getString("taskId");
            String url = payload.getString("url");
            if (taskId.isEmpty() || !"https".equals(Uri.parse(url).getScheme())) {
                throw new IllegalArgumentException("invalid_slide_fixture");
            }
            WindowAppParam app = WindowAppParam.createSlideApp(taskId, url,
                    payload.optString("title", "Initialization benchmark"));
            app.setOriginSize(new WindowOriginSize(1280, 720));
            room.addApp(app, new Promise<String>() {
                @Override public void then(String appId) { emit("SLIDE_ADDED", appId, requestId); }
                @Override public void catchEx(SDKError error) { emit("ERROR", "add_slide_failed", requestId); }
            });
        } catch (Exception error) {
            emit("ERROR", "invalid_slide_payload", requestId);
        }
    }

    private static String required(Intent intent, String key) {
        String value = intent.getStringExtra(key);
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("missing_" + key);
        return value;
    }

    public final class SourceProbe {
        @JavascriptInterface public void record(String json) {
            if (destroyed) return;
            try {
                JSONObject source = new JSONObject(json);
                String stage = source.getString("stage");
                if (!stage.matches("T[1-4]|T5_SETUP|T5_READY")) return;
                JSONObject event = baseEvent(stage, source.getLong("deviceEpochMs"));
                event.put("performanceMs", source.getDouble("performanceMs"));
                if (source.has("appId")) event.put("appId", source.getString("appId"));
                if (source.has("taskId")) event.put("taskId", source.getString("taskId"));
                Log.i(TAG, event.toString());
            } catch (Exception error) {
                emit("ERROR", "invalid_source_probe", null);
            }
        }
    }

    private JSONObject baseEvent(String stage, long epochMs) throws Exception {
        JSONObject event = new JSONObject();
        event.put("runId", runId);
        event.put("sdk", "1.0");
        event.put("stage", stage);
        event.put("deviceEpochMs", epochMs);
        return event;
    }

    private void emit(String stage, String detail, String requestId) {
        if (destroyed) return;
        try {
            JSONObject event = baseEvent(stage, System.currentTimeMillis());
            event.put("elapsedRealtimeMs", SystemClock.elapsedRealtime());
            if (detail != null) event.put("detail", detail);
            if (requestId != null) event.put("requestId", requestId);
            Log.i(TAG, event.toString());
        } catch (Exception ignored) { /* No credentials or arbitrary errors in benchmark output. */ }
    }

    /** Install SourceProbe before the original file load; preserve DWebView's startup queue. */
    private static final class BenchmarkView extends WhiteboardView {
        private String initialUrl;
        private boolean started;
        private Runnable prepared, failed;
        BenchmarkView(Context context) { super(context); }
        @Override public void loadUrl(String url) {
            if (!started) initialUrl = url;
            else super.loadUrl(url);
        }
        void startEntry() {
            started = true;
            super.loadUrl(initialUrl);
        }
        @Override public <T> void callHandler(String method, Object[] args, OnReturnValue<T> handler) {
            if (!"sdk.newWhiteSdk".equals(method) || prepared == null) { super.callHandler(method, args, handler); return; }
            super.callHandler(method, args, (OnReturnValue<T>) value -> {
                if (handler != null) handler.onValue(value);
                boolean success = false;
                try {
                    Object payload = new org.json.JSONTokener(String.valueOf(value)).nextValue();
                    if (payload instanceof JSONObject && ((JSONObject)payload).has("value")) payload = ((JSONObject)payload).get("value");
                    if (payload instanceof String) payload = new org.json.JSONTokener((String)payload).nextValue();
                    success = payload instanceof JSONObject && !((JSONObject)payload).has("__error");
                } catch (Exception ignored) { }
                Runnable done = success ? prepared : failed;
                prepared = null; failed = null;
                if (done != null) done.run();
            });
        }
    }

    @Override public void onDestroy() {
        destroyed = true;
        if (sdk != null) sdk.releaseRoom();
        if (view != null) {
            view.removeJavascriptInterface("__wbInitBenchNative");
            view.destroy();
        }
        room = null;
        super.onDestroy();
    }
}
