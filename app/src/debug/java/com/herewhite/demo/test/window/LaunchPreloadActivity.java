package com.herewhite.demo.test.window;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.herewhite.demo.R;
import com.herewhite.demo.StartActivity;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.AppliancePluginOptions;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.SlideSyncEventQueuePolicy;
import com.herewhite.sdk.domain.WindowOriginSize;
import com.herewhite.sdk.domain.WindowParams;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;
import wendu.dsbridge.special.OnReturnValue;

/** Debug Demo: the launch screen covers an attached, full-size WhiteboardView. */
public final class LaunchPreloadActivity extends StartActivity {
    private String runId;
    private PreloadView whiteboard;
    private WhiteSdk sdk;
    private Room room;
    private RoomParams params;
    private View launchScreen;
    private FrameLayout layers;
    private TextView status;
    private Button joinButton, directButton;
    private boolean preloadMode;
    private boolean ready, joining;
    private volatile boolean destroyed;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        preloadMode = !"direct".equals(getIntent().getStringExtra("mode"));
        runId = getIntent().getStringExtra("runId");
        if (runId == null) runId = "manual-" + UUID.randomUUID();
        ViewGroup content = findViewById(android.R.id.content);
        launchScreen = content.getChildAt(0);
        content.removeView(launchScreen);
        launchScreen.setBackgroundColor(Color.WHITE);
        // Keep the real WebView attached and VISIBLE, covered by opaque native UI.
        // GONE, onPause(), or creating another WebView at join would defeat preload.
        layers = new FrameLayout(this);
        layers.addView(launchScreen, new FrameLayout.LayoutParams(-1, -1));
        content.addView(layers, new ViewGroup.LayoutParams(-1, -1));
        LinearLayout buttons = findViewById(R.id.container);
        status = new TextView(this);
        status.setText(preloadMode ? "正在后台准备白板…" : "选择普通入房后开始初始化白板");
        buttons.addView(status, 0);
        joinButton = new Button(this);
        joinButton.setText("预加载入房");
        joinButton.setContentDescription("launch-preload-join");
        joinButton.setEnabled(false);
        buttons.addView(joinButton, 1);
        joinButton.setOnClickListener(v -> joinPreparedRoom());
        directButton = new Button(this);
        directButton.setText("普通入房（点击后初始化）");
        directButton.setContentDescription("launch-direct-join");
        buttons.addView(directButton, 2);
        directButton.setOnClickListener(v -> joinDirectRoom());
        if (preloadMode) initializeWhiteboard(true);
        buttons.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                Button target = preloadMode ? joinButton : directButton;
                if (target.getWidth() <= 0 || target.getHeight() <= 0) return;
                buttons.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (destroyed) return;
                try {
                    JSONObject e = base("PAGE_READY", System.currentTimeMillis());
                    e.put("joinButtonBounds", buttonBounds(target));
                    e.put("sdkExists", sdk != null); e.put("webViewExists", whiteboard != null);
                    Log.i("WB_INIT_BENCH", e.toString());
                } catch (Exception ignored) { emit("ERROR", "invalid_page_probe"); }
            }
        });
    }

    private void initializeWhiteboard(boolean preparing) {
        try {
            whiteboard = new PreloadView(this);
            whiteboard.addJavascriptInterface(new Probe(), "__wbInitBenchNative");
            whiteboard.getSettings().setAllowUniversalAccessFromFileURLs(true);
            layers.addView(whiteboard, 0, new FrameLayout.LayoutParams(-1, -1));
            JSONObject credentials = credentials();
            WhiteSdkConfiguration config = new WhiteSdkConfiguration(credentials.getString("appIdentifier"), true);
            config.setRegion(Region.cn);
            if (credentials.has("apiHosts")) {
                java.util.ArrayList<String> apiHosts = new java.util.ArrayList<>();
                org.json.JSONArray hosts = credentials.getJSONArray("apiHosts");
                for (int i = 0; i < hosts.length(); i++) apiHosts.add(hosts.getString(i));
                config.setApiHosts(apiHosts);
            }
            config.setUseMultiViews(true);
            config.setEnableAppliancePlugin(true);
            // Debug-only timing control: both groups enable the real plugin.
            // The late group's constructor preparation is skipped by benchmark JS.
            boolean applianceEarly = "early".equals(getIntent().getStringExtra("appliancePreparationMode"));
            if (applianceEarly) config.setAppliancePluginOptions(new AppliancePluginOptions());
            // Direct control explicitly skips all optional App class warmup.
            // Prepared entry omits the list: Bridge chooses Slide + Presentation.
            boolean directDefaultAppWarmup = getIntent().getBooleanExtra("directDefaultAppWarmup", false);
            if ((!preparing && !directDefaultAppWarmup) || getIntent().getBooleanExtra("skipAppWarmup", false)) {
                config.setPreloadBuiltinApps(java.util.Collections.emptyList());
            }
            // Default keeps the model option omitted. Only the dev model A/B arm opts in.
            if (getIntent().getBooleanExtra("enableModelPreload", false)) config.setEnableModelPreload(true);
            config.setPreloadDynamicPPT(false);
            config.getSlideAppOptions().setEnableScale(true);
            config.getSlideAppOptions().setEnableGlobalClick(false);
            config.getSlideAppOptions().setSyncEventQueuePolicy(SlideSyncEventQueuePolicy.LatestPendingRender);
            String uid = getIntent().getStringExtra("uid");
            if (uid == null) uid = "launch-preload-" + UUID.randomUUID();
            params = new RoomParams(credentials.getString("uuid"), credentials.getString("roomToken"), uid);
            params.setRegion(Region.cn);
            params.setWritable(getIntent().getBooleanExtra("seed", false));
            // Both entries share room/render settings; only optional App warmup differs.
            // Keep the original options location in RoomParams (empty means default auto).
            if (!applianceEarly) params.setAppliancePluginOptions(new AppliancePluginOptions());
            params.setWindowParams(new WindowParams().setForceMaximized(true)
                    .setLazySetupInMaximizedMode(true).setMaxCachedAppsInMaximizedMode(4)
                    .setUseBoxesStatus(false).setOriginSize(new WindowOriginSize(1280, 720)));
            if (preparing) whiteboard.setupComplete = () -> runOnUiThread(() -> {
                if (!destroyed && preloadMode) whiteboard.evaluateJavascript("window.__launchPrepare()", null);
            });
            whiteboard.startEntry();
            emit(preparing ? "T-1" : "SDK_CONSTRUCT_BEGIN", null);
            sdk = new WhiteSdk(whiteboard, this, config);
        } catch (Exception error) {
            status.setText("请先配置测试房间凭据");
            emit("ERROR", "init_" + error.getClass().getSimpleName());
        }
    }

    private void joinPreparedRoom() {
        if (destroyed || !ready || joining) return;
        emit("CLICK", null);
        joining = true;
        // The preloaded path keeps the same SDK and WebView.
        beginJoin();
    }

    private void joinDirectRoom() {
        if (destroyed || joining) return;
        emit("CLICK", null);
        joining = true;
        preloadMode = false;
        // In benchmark direct mode these are null: no WebView/SDK existed before the tap.
        // Manual selection from the preloading page also creates a fresh instance here.
        disposeWhiteboard();
        initializeWhiteboard(false);
        if (sdk != null) beginJoin();
    }

    private void beginJoin() {
        joinButton.setEnabled(false);
        directButton.setEnabled(false);
        launchScreen.setVisibility(View.GONE);
        emit("T0", null);
        sdk.joinRoom(params, new AbstractRoomCallbacks() {}, new Promise<Room>() {
            @Override public void then(Room value) {
                if (destroyed) return;
                room = value;
                emit("NATIVE_JOIN_SUCCESS", null);
            }
            @Override public void catchEx(SDKError error) {
                if (destroyed) return;
                emit("ERROR", "join_failed");
                launchScreen.setVisibility(View.VISIBLE);
                status.setText("入房失败，请返回重试");
            }
        });
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ("join".equals(intent.getStringExtra("action"))) {
            if (preloadMode) joinButton.performClick(); else directButton.performClick();
        }
        else if ("leave".equals(intent.getStringExtra("action")) && room != null) {
            room.disconnect(new Promise<Object>() {
                @Override public void then(Object value) { room = null; emit("LEFT", null); }
                @Override public void catchEx(SDKError error) { emit("ERROR", "leave_failed"); }
            });
        }
    }

    private JSONObject credentials() throws Exception {
        try (FileInputStream in = new FileInputStream(new File(getApplicationInfo().dataDir, "launch-preload.private.json"));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[2048]; int count;
            while ((count = in.read(b)) > 0) out.write(b, 0, count);
            return new JSONObject(out.toString("UTF-8"));
        }
    }

    public final class Probe {
        @JavascriptInterface public boolean shouldDeferAppliancePreparation() {
            return "late".equals(getIntent().getStringExtra("appliancePreparationMode"));
        }
        @JavascriptInterface public void prepared(String json) {
            runOnUiThread(() -> {
                if (destroyed || ready || !preloadMode) return;
                try {
                    JSONObject evidence = new JSONObject(json);
                    JSONObject e = base("PRELOAD_READY", evidence.getLong("readyAt"));
                    e.put("evidence", evidence);
                    e.put("joinButtonBounds", buttonBounds(joinButton));
                    Log.i("WB_INIT_BENCH", e.toString());
                    ready = true;
                    status.setText("白板已准备，可直接入房");
                    joinButton.setEnabled(true);
                } catch (Exception ignored) { emit("ERROR", "invalid_preload_probe"); }
            });
        }
        @JavascriptInterface public void record(String json) {
            if (destroyed) return;
            try {
                JSONObject source = new JSONObject(json);
                String stage = source.getString("stage");
                if (!stage.matches("T[1-4]|T5_SETUP|T5_READY")) return;
                source.put("runId", runId); source.put("mode", preloadMode ? "preload" : "direct");
                Log.i("WB_INIT_BENCH", source.toString());
            } catch (Exception ignored) { emit("ERROR", "invalid_timing_probe"); }
        }
    }

    private JSONObject base(String stage, long epoch) throws Exception {
        JSONObject e = new JSONObject();
        e.put("stage", stage); e.put("runId", runId); e.put("mode", preloadMode ? "preload" : "direct");
        e.put("deviceEpochMs", epoch); e.put("elapsedRealtimeMs", SystemClock.elapsedRealtime());
        return e;
    }
    private void emit(String stage, String detail) {
        if (destroyed) return;
        try {
            JSONObject e = base(stage, System.currentTimeMillis());
            if (detail != null) e.put("detail", detail);
            Log.i("WB_INIT_BENCH", e.toString());
        } catch (Exception ignored) { }
    }

    private static final class PreloadView extends WhiteboardView {
        private String entry;
        private boolean started;
        private Runnable setupComplete;
        PreloadView(Context context) { super(context); }
        @Override public void loadUrl(String url) { if (started) super.loadUrl(url); else entry = url; }
        void startEntry() { started = true; super.loadUrl(entry); }
        @Override public <T> void callHandler(String method, Object[] args, OnReturnValue<T> handler) {
            if (!"sdk.newWhiteSdk".equals(method) || setupComplete == null) {
                super.callHandler(method, args, handler); return;
            }
            Runnable complete = setupComplete; setupComplete = null;
            super.callHandler(method, args, (OnReturnValue<T>) value -> {
                if (handler != null) handler.onValue(value);
                complete.run();
            });
        }
    }
    private JSONObject buttonBounds(Button button) throws Exception {
        int[] location = new int[2]; button.getLocationOnScreen(location);
        JSONObject bounds = new JSONObject();
        bounds.put("x", location[0]); bounds.put("y", location[1]);
        bounds.put("width", button.getWidth()); bounds.put("height", button.getHeight());
        return bounds;
    }

    private void disposeWhiteboard() {
        if (sdk != null) { sdk.releaseRoom(); sdk = null; }
        if (whiteboard != null) {
            whiteboard.removeJavascriptInterface("__wbInitBenchNative");
            if (whiteboard.getParent() != null) ((ViewGroup)whiteboard.getParent()).removeView(whiteboard);
            whiteboard.destroy(); whiteboard = null;
        }
        room = null;
    }

    @Override protected void onDestroy() {
        destroyed = true;
        disposeWhiteboard();
        super.onDestroy();
    }
}
