
package com.herewhite.demo.test;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.google.gson.Gson;
import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.DemoAPI;
import com.herewhite.demo.LocalFileWebViewClient;
import com.herewhite.demo.R;
import com.herewhite.demo.WhiteWebViewClient;
import com.herewhite.demo.utils.MapBuilder;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.CommonCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.FontFace;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WhiteDisplayerState;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import wendu.dsbridge.DWebView;


public class RatioChangeActivity extends BaseActivity {
    static final String TAG = RatioChangeActivity.class.getSimpleName();

    /**
     * 和 iOS 名字一致
     */
    private static final String EVENT_NAME = "WhiteCommandCustomEvent";
    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";

    final Gson gson = new Gson();
    final DemoAPI demoAPI = new DemoAPI();

    WhiteboardView mWhiteboardView;
    @VisibleForTesting
    WhiteSdk mWhiteSdk;
    @VisibleForTesting
    Room mRoom;

    /**
     * 自定义 GlobalState 示例
     * 继承自 GlobalState 的子类，然后调用 {@link WhiteDisplayerState#setCustomGlobalStateClass(Class)}
     */
    class MyGlobalState extends GlobalState {
        public String getOne() {
            return one;
        }

        public void setOne(String one) {
            this.one = one;
        }

        String one;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratio_change);

        mWhiteboardView = findViewById(R.id.white);
        DWebView.setWebContentsDebuggingEnabled(true);
        mWhiteboardView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        // 使用阿里云的 HttpDns，避免 DNS 污染等问题
        useHttpDnsService(false);

        // 使用 LocalFileWebViewClient 对 动态 ppt 拦截进行替换，先查看本地是否有，如果没有再发出网络请求
        LocalFileWebViewClient client = new LocalFileWebViewClient();
        client.setPptDirectory(getCacheDir().getAbsolutePath());
        mWhiteboardView.setWebViewClient(client);

        getRoomToken(demoAPI.getDemoUUID());

        initSystemUI();

        findViewById(R.id.ratio).setOnClickListener(v -> {
            currentRatioType = (currentRatioType + 1) % 3;
            updateLayout();
        });

        findViewById(R.id.scalePpt).setOnClickListener(v -> {
            mRoom.scalePptToFit();
        });

        View videoArea = findViewById(R.id.videoArea);
        findViewById(R.id.videoSwitch).setOnClickListener(v -> {
            mRoom.scalePptToFit();
            if (videoArea.getVisibility() == View.VISIBLE) {
                videoArea.setVisibility(View.GONE);
            } else {
                videoArea.setVisibility(View.VISIBLE);
            }
        });

        // 监听父布局变更更新
        findViewById(R.id.parentLayout).addOnLayoutChangeListener((v1, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            updateLayout();
        });
    }

    private static final int RATIO_TYPE_1_1 = 0;
    private static final int RATIO_TYPE_16_9 = 1;
    private static final int RATIO_TYPE_4_3 = 2;

    private int currentRatioType = 0;

    private void updateLayout() {
        ViewGroup.LayoutParams layoutParams = mWhiteboardView.getLayoutParams();
        int height = mWhiteboardView.getHeight();
        switch (currentRatioType) {
            case RATIO_TYPE_1_1:
                layoutParams.width = height;
                break;
            case RATIO_TYPE_16_9:
                layoutParams.width = height * 16 / 9;
                break;
            case RATIO_TYPE_4_3:
                layoutParams.width = height * 4 / 3;
                break;
            default:
                break;
        }
        mWhiteboardView.setLayoutParams(layoutParams);
    }

    private void initSystemUI() {
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility((View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN));
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                // or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void getRoomToken(final String uuid) {
        demoAPI.getRoomToken(uuid, new DemoAPI.Result() {
            @Override
            public void success(String uuid, String roomToken) {
                joinRoom(uuid, roomToken);
            }

            @Override
            public void fail(String message) {
                alert("获取房间 token 失败", message);
            }
        });
    }

    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);

        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        /*显示用户头像*/
        configuration.setUserCursor(true);
        //动态 ppt 需要的自定义字体，如果没有使用，无需调用
        configuration.setFonts(new MapBuilder<String, String>().put("宋体", "https://your-cdn.com/Songti.ttf").build());

        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);

        //图片替换 API，需要在 whiteSDKConfig 中先行调用 setHasUrlInterrupterAPI，进行设置，否则不会被回调。
        mWhiteSdk.setCommonCallbacks(new CommonCallbacks() {
            @Override
            public String urlInterrupter(String sourceUrl) {
                return sourceUrl;
            }

            @Override
            public void onMessage(JSONObject message) {
                Log.d(TAG, message.toString());
            }

            @Override
            public void sdkSetupFail(SDKError error) {
                Log.e(TAG, "sdkSetupFail " + error.toString());
            }

            @Override
            public void throwError(Object args) {
                Log.e(TAG, "throwError " + args);
            }

            @Override
            public void onPPTMediaPlay() {
                logAction();
            }

            @Override
            public void onPPTMediaPause() {
                logAction();
            }
            // 如果用户需要用到 rtc 混音功能来解决回声和声音抑制问题，那么必须要在 whiteSDK 之前初始化 rtcEngine
            // AudioMixerBridgeImpl 在传入 sdk 后，ppt 内的音视频就全部使用 rtc 混音的方式播放
        });

        FontFace fontFace = new FontFace("example", "url(https://white-pan.oss-cn-shanghai.aliyuncs.com/Pacifico-Regular.ttf)");
        // mWhiteSdk.setupFontFaces(new FontFace[]{fontFace});
        mWhiteSdk.loadFontFaces(new FontFace[]{fontFace}, new Promise<JSONObject>() {
            @Override
            public void then(JSONObject object) {
                logRoomInfo("loadFontFaces");
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });

        /** 设置自定义全局状态，在后续回调中 GlobalState 直接进行类型转换即可 */
        WhiteDisplayerState.setCustomGlobalStateClass(MyGlobalState.class);

        //如需支持用户头像，请在设置 WhiteSdkConfiguration 后，再调用 setUserPayload 方法，传入符合用户信息
        RoomParams roomParams = new RoomParams(uuid, token);

        final Date joinDate = new Date();
        logRoomInfo("native join " + joinDate);
        mWhiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                //记录加入房间消耗的时长
                logRoomInfo("native join in room duration: " + (System.currentTimeMillis() - joinDate.getTime()) / 1000f + "s");
                mRoom = room;
            }

            @Override
            public void catchEx(SDKError t) {
                logRoomInfo("native join fail: " + t.getMessage());
                showToast(t.getMessage());
            }
        });
    }
    //endregion

    //region private
    private void alert(final String title, final String detail) {
        runOnUiThread(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(RatioChangeActivity.this).create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(detail);
            alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL,
                    "OK",
                    (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });
            alertDialog.show();
        });
    }

    private void useHttpDnsService(boolean use) {
        if (use) {
            /** 直接使用此 id 即可，sdk 已经在阿里云 HttpDns 后台做过配置 */
            HttpDnsService httpDnsService = HttpDns.getService(getApplicationContext(), "188301");
            httpDnsService.setPreResolveHosts(new ArrayList<>(Arrays.asList("expresscloudharestoragev2.herewhite.com", "cloudharev2.herewhite.com", "scdncloudharestoragev3.herewhite.com", "cloudcapiv4.herewhite.com")));
            mWhiteboardView.setWebViewClient(new WhiteWebViewClient(httpDnsService));
        }
    }
    //endregion

    //region log
    void logRoomInfo(String str) {
        Log.i(ROOM_INFO, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction(String str) {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction() {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName());
    }

    void showToast(Object o) {
        Log.i("showToast", o.toString());
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }
    //endregion

    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }
}
