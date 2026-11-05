package com.herewhite.demo.test.window;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import com.herewhite.demo.R;
import com.herewhite.demo.common.SampleBaseActivity;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** Local, debug-only experiment. Uses the configured test room and packaged SDK. */
public class GifExperimentActivity extends WindowAppliancePluginActivity {
    private static final String TAG = "GifExperiment";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String script;
    private String mode = "baseline";
    private boolean stopped;
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (stopped) return;
            whiteboardView.evaluateJavascript(script + ";window.__gifExperiment.sample()", null);
            handler.postDelayed(this, 1000);
        }
    };
    public final class ProbeInterface {
        @JavascriptInterface public void event(String message) {
            for (int offset = 0; offset < message.length(); offset += 3000) {
                Log.i(TAG, message.substring(offset, Math.min(offset + 3000, message.length())));
            }
        }
    }
    @Override protected void onCreate(Bundle state) {
        getIntent().putExtra(SampleBaseActivity.EXTRA_ROOM_UUID, getString(R.string.room_uuid));
        getIntent().putExtra(SampleBaseActivity.EXTRA_ROOM_TOKEN, getString(R.string.room_token));
        try (InputStream input = getAssets().open("gif-experiment.js")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) out.write(buffer, 0, count);
            script = out.toString("UTF-8");
        } catch (Exception error) { throw new IllegalStateException(error); }
        super.onCreate(state);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        whiteboardView.addJavascriptInterface(new ProbeInterface(), "__gifNative");
        handler.postDelayed(tick, 1000);
    }
    @Override protected void onJoinRoomSuccess() {
        super.onJoinRoomSuccess();
        handler.postDelayed(() -> applyMode(getIntent()), 1000);
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyMode(intent);
    }
    private void applyMode(Intent intent) {
        mode = intent.getStringExtra("mode");
        if (mode == null) mode = "baseline";
        whiteboardView.setLayerType("software".equals(mode) ? View.LAYER_TYPE_SOFTWARE : View.LAYER_TYPE_NONE, null);
        Log.i(TAG, "nativeMode=" + mode + " hardware=" + whiteboardView.isHardwareAccelerated());
        whiteboardView.evaluateJavascript(script + ";window.__gifExperiment.apply(" + JSONObject.quote(mode) + ")", null);
    }
    @Override protected void onDestroy() {
        stopped = true;
        handler.removeCallbacksAndMessages(null);
        whiteboardView.removeJavascriptInterface("__gifNative");
        super.onDestroy();
    }
}
