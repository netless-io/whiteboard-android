package com.herewhite.demo.test.window;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.LruCache;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.R;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomCallbacks;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.AppState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.SlidePageState;
import com.herewhite.sdk.domain.WindowAppParam;
import com.herewhite.sdk.domain.WindowAppSyncAttrs;
import com.herewhite.sdk.domain.WindowDocsEvent;
import com.herewhite.sdk.domain.WindowParams;
import com.herewhite.sdk.window.SlideListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MaoCustomWindowActivity extends BaseActivity {
    private static final String APP_ID = "123/123";
    private static final String SLIDE_PREFIX = "https://white-cover.oss-cn-hangzhou.aliyuncs.com/flat/dynamicConvert";
    private static final String SLIDE_TASK_ID = "46e8ff5db5714fec818f5594a6c55083";
    private static final int FALLBACK_PAGE_COUNT = 12;

    private WhiteboardView whiteboardView;
    private WhiteSdk whiteSdk;
    private Room room;
    private LinearLayout appBar;
    private LinearLayout previewBar;
    private ImageButton closeAppButton;
    private TextView pageStateView;
    private TextView logView;
    private String currentSlideAppId;
    private int currentPage = 1;
    private int pageCount = FALLBACK_PAGE_COUNT;
    private final OkHttpClient imageClient = new OkHttpClient();
    private final Map<String, WindowAppSyncAttrs> apps = new HashMap<>();
    private final LruCache<String, Bitmap> previewCache = new LruCache<String, Bitmap>(24) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return 1;
        }
    };
    private final Set<String> loadingPreviewKeys = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mao_custom_window);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        whiteboardView = findViewById(R.id.white);
        appBar = findViewById(R.id.appBar);
        previewBar = findViewById(R.id.previewBar);
        closeAppButton = findViewById(R.id.closeApp);
        pageStateView = findViewById(R.id.pageState);
        logView = findViewById(R.id.logDisplay);

        setupControls();
        renderPreviewBar(pageCount);
        setupRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (whiteboardView != null) {
            whiteboardView.destroy();
        }
    }

    private void setupControls() {
        findViewById(R.id.boxNormal).setOnClickListener(v -> {
            if (room != null) room.setWindowBoxState("normal");
        });
        findViewById(R.id.boxMax).setOnClickListener(v -> {
            if (room != null) room.setWindowBoxState("maximized");
        });
        findViewById(R.id.boxMin).setOnClickListener(v -> {
            if (room != null) room.setWindowBoxState("minimized");
        });
        closeAppButton.setOnClickListener(v -> closeCurrentApp());
        findViewById(R.id.addSlideApp).setOnClickListener(v -> {
            if (room != null) addSlideApp();
        });
        findViewById(R.id.prevPage).setOnClickListener(v -> dispatch(WindowDocsEvent.PrevPage));
        findViewById(R.id.nextPage).setOnClickListener(v -> dispatch(WindowDocsEvent.NextPage));
    }

    private void setupRoom() {
        DemoAPI demoAPI = DemoAPI.get();
        DemoAPI.Result result = new DemoAPI.Result() {
            @Override
            public void success(String uuid, String token) {
                joinRoom(uuid, token);
            }

            @Override
            public void fail(String message) {
                log("create room failed: " + message);
                showAlert("创建房间失败", message);
            }
        };

        String uuid = demoAPI.getRoomUUID();
        if (uuid != null && uuid.length() > 0) {
            demoAPI.getRoomToken(uuid, result);
        } else {
            demoAPI.getNewRoom(result);
        }
    }

    private void joinRoom(String uuid, String token) {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(APP_ID, true);
        configuration.setRegion(Region.cn);
        configuration.setUseMultiViews(true);

        whiteSdk = new WhiteSdk(whiteboardView, this, configuration);
        whiteSdk.setSlideListener(new SlideListener() {
            @Override
            public void onSlidePageStateChanged(String appId, int page, int total) {
                runOnUiThread(() -> updateSlidePageState(appId, page, total));
            }
        });

        RoomParams params = new RoomParams(uuid, token, deviceUserId());
        params.setRegion(Region.cn);
        params.setWritable(true);
        params.setWindowParams(new WindowParams()
                .setContainerSizeRatio(9f / 16)
                .setFullscreen(true));

        whiteSdk.joinRoom(params, roomCallbacks, new Promise<Room>() {
            @Override
            public void then(Room joinedRoom) {
                room = joinedRoom;
                log("joined room: " + uuid);
                addSlideApp();
            }

            @Override
            public void catchEx(SDKError error) {
                log("join failed: " + error.getMessage());
            }
        });
    }

    private final RoomCallbacks roomCallbacks = new AbstractRoomCallbacks() {
        @Override
        public void onPhaseChanged(RoomPhase phase) {
            log("phase: " + phase.name());
        }

        @Override
        public void onRoomStateChanged(RoomState state) {
            runOnUiThread(() -> {
                if (state.getWindowBoxState() != null) {
                    log("windowBoxState: " + state.getWindowBoxState());
                }
                AppState appState = state.getAppState();
                if (appState != null) {
                    String previousAppId = currentSlideAppId;
                    String focusedId = appState.getFocusedId();
                    currentSlideAppId = focusedId;
                    refreshApps();
                    if (focusedId == null) {
                        currentPage = 1;
                        pageCount = FALLBACK_PAGE_COUNT;
                        pageStateView.setText("0/0");
                        renderPreviewBar(pageCount);
                    } else if (!focusedId.equals(previousAppId)) {
                        querySlidePageState();
                    }
                }
            });
        }
    };

    private void addSlideApp() {
        WindowAppParam param = WindowAppParam.createSlideApp(SLIDE_TASK_ID, SLIDE_PREFIX, "Mao Slide");
        room.addApp(param, new Promise<String>() {
            @Override
            public void then(String appId) {
                currentSlideAppId = appId;
                log("add slide: " + appId);
                refreshApps();
                querySlidePageState();
            }

            @Override
            public void catchEx(SDKError error) {
                log("add slide failed: " + error.getMessage());
            }
        });
    }

    private void refreshApps() {
        if (room == null) return;
        room.queryAllApps(new Promise<Map<String, WindowAppSyncAttrs>>() {
            @Override
            public void then(Map<String, WindowAppSyncAttrs> result) {
                runOnUiThread(() -> {
                    apps.clear();
                    apps.putAll(result);
                    renderAppBar();
                });
            }

            @Override
            public void catchEx(SDKError error) {
                log("query apps failed: " + error.getMessage());
            }
        });
    }

    private void querySlidePageState() {
        if (room == null) return;
        room.querySlidePageState(currentSlideAppId, new Promise<SlidePageState>() {
            @Override
            public void then(SlidePageState state) {
                runOnUiThread(() -> updateSlidePageState(state.getAppId(), state.getPage(), state.getPageCount()));
            }

            @Override
            public void catchEx(SDKError error) {
                log("query slide page failed: " + error.getMessage());
            }
        });
    }

    private void renderAppBar() {
        appBar.removeAllViews();
        if (apps.isEmpty()) {
            appBar.addView(label("No apps", false));
            return;
        }
        for (Map.Entry<String, WindowAppSyncAttrs> entry : apps.entrySet()) {
            String appId = entry.getKey();
            Button button = new Button(this);
            button.setText((appId.equals(currentSlideAppId) ? "* " : "") + appId);
            button.setAllCaps(false);
            button.setBackgroundColor(appId.equals(currentSlideAppId) ? Color.rgb(204, 230, 255) : Color.TRANSPARENT);
            button.setOnClickListener(v -> {
                currentSlideAppId = appId;
                room.focusApp(appId);
                renderAppBar();
                querySlidePageState();
            });
            appBar.addView(button, appBarParams());
        }
    }

    private void closeCurrentApp() {
        if (currentSlideAppId == null) {
            log("no focused app to close");
            return;
        }
        closeApp(currentSlideAppId);
    }

    private void closeApp(String appId) {
        if (room == null) return;
        room.closeApp(appId, new Promise<Boolean>() {
            @Override
            public void then(Boolean success) {
                log("close app " + appId + ": " + success);
                refreshApps();
            }

            @Override
            public void catchEx(SDKError error) {
                log("close app failed: " + error.getMessage());
            }
        });
    }

    private TextView label(String text, boolean selected) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setGravity(Gravity.CENTER);
        label.setTextColor(Color.rgb(39, 54, 74));
        label.setBackgroundColor(selected ? Color.rgb(204, 230, 255) : Color.TRANSPARENT);
        return label;
    }

    private LinearLayout.LayoutParams appBarParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(6, 0, 6, 0);
        return params;
    }

    private void renderPreviewBar(int total) {
        previewBar.removeAllViews();
        int count = Math.max(total, 1);
        for (int page = 1; page <= count; page++) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setBackgroundColor(page == currentPage ? Color.rgb(204, 230, 255) : Color.WHITE);
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            TextView label = label(String.valueOf(page), page == currentPage);
            item.addView(image, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    48
            ));
            item.addView(label, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            int targetPage = page;
            item.setOnClickListener(v -> dispatch(WindowDocsEvent.JumpToPage(targetPage)));
            previewBar.addView(item, previewParams(page == currentPage));
            loadPreview(image, page);
        }
    }

    private LinearLayout.LayoutParams previewParams(boolean selected) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(132, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(4, selected ? 0 : 6, 4, selected ? 0 : 6);
        return params;
    }

    private String previewUrl(int pageIndex) {
        return SLIDE_PREFIX + "/" + SLIDE_TASK_ID + "/preview/" + pageIndex + ".png";
    }

    private String previewCacheKey(int pageIndex) {
        return previewUrl(pageIndex);
    }

    private String previewFallbackCacheKey(int pageIndex) {
        return SLIDE_TASK_ID + ":" + pageIndex;
    }

    private void loadPreview(ImageView target, int pageIndex) {
        String cacheKey = previewCacheKey(pageIndex);
        target.setTag(cacheKey);

        Bitmap cached = previewCache.get(cacheKey);
        if (cached == null && currentSlideAppId != null) {
            cached = previewCache.get(previewFallbackCacheKey(pageIndex));
            if (cached != null) {
                previewCache.put(cacheKey, cached);
            }
        }
        if (cached != null) {
            target.setImageBitmap(cached);
            return;
        }

        target.setImageDrawable(null);
        if (!loadingPreviewKeys.add(cacheKey)) {
            return;
        }

        String url = previewUrl(pageIndex);
        new Thread(() -> {
            try {
                Request request = new Request.Builder().url(url).build();
                try (Response response = imageClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        return;
                    }
                    byte[] bytes = response.body().bytes();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bitmap != null) {
                        previewCache.put(cacheKey, bitmap);
                        runOnUiThread(() -> {
                            if (cacheKey.equals(target.getTag())) {
                                target.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                log("preview load failed: " + e.getMessage());
            } finally {
                loadingPreviewKeys.remove(cacheKey);
            }
        }).start();
    }

    private void updateSlidePageState(String appId, int page, int total) {
        currentSlideAppId = appId;
        currentPage = Math.max(page, 1);
        pageCount = Math.max(total, 1);
        pageStateView.setText(currentPage + "/" + pageCount);
        renderPreviewBar(pageCount);
    }

    private void dispatch(WindowDocsEvent event) {
        if (room == null) return;
        room.dispatchDocsEvent(event, new Promise<Boolean>() {
            @Override
            public void then(Boolean success) {
                log("dispatch " + event.getEvent() + ": " + success);
            }

            @Override
            public void catchEx(SDKError error) {
                log("dispatch failed: " + error.getMessage());
            }
        });
    }

    private String deviceUserId() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId == null ? "mao-custom-window-demo" : androidId;
    }

    private void log(String message) {
        runOnUiThread(() -> {
            String text = message + "\n" + logView.getText();
            logView.setText(text);
        });
    }
}
