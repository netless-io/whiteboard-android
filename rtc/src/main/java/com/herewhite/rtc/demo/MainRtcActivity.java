package com.herewhite.rtc.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomListener;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WhiteDisplayerState;
import com.herewhite.sdk.domain.WindowParams;

import org.json.JSONObject;

import java.util.HashMap;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class MainRtcActivity extends AppCompatActivity {
    private static final String APP_ID = "123/12312313";
    private static final String ROOM_UUID = "2b612720510f11ed9fbf251960392aac";
    private static final String ROOM_TOKEN = "NETLESSROOM_YWs9c21nRzh3RzdLNk1kTkF5WCZub25jZT0yYmFlNWNjMC01MTBmLTExZWQtODZiOS1hMzhmMWY5ZjcyYmMmcm9sZT0xJnNpZz03ZDg2MGExMjU2Yjc3NDcwNDQxZDc4ZmMwYTQyZTcyMTc5YWNiMzAxZDZlYzk2NGQ0NjdhMzU4MWQyNmEyZjMwJnV1aWQ9MmI2MTI3MjA1MTBmMTFlZDlmYmYyNTE5NjAzOTJhYWM";
    private static final String DEFAULT_UID = "5e62a5c0";

    private static final int PERMISSION_REQ_ID = 22;
    // 如果需要保存 rtc 日志到 sdk 卡就需要 WRITE_EXTERNAL_STORAGE 权限
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    // rtc 远端视频窗口
    private FrameLayout mLocalContainer;
    // rtc 本地视频窗口
    private FrameLayout mRemoteContainer;
    private SurfaceView mLocalView;
    private SurfaceView mRemoteView;
    private ImageView mCallBtn;
    // rtc 客户端
    private RtcEngine mRtcEngine;
    private boolean mCallEnd = true;

    // Whiteboard
    private WhiteboardView whiteboardView;
    private WhiteSdk whiteSdk;
    private Room room;
    private AudioMixerBridgeImpl audioMixerBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_rtc);

        WebView.setWebContentsDebuggingEnabled(true);
        mCallBtn = findViewById(R.id.btn_call);
        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);
        whiteboardView = findViewById(R.id.whiteboardView);

        // 如果用户需要用到 rtc 混音功能来解决回声和声音抑制问题，那么必须要在 whiteSDK 之前初始化 rtcEngine
        checkAndInitRtcEngine();


        findViewById(R.id.exitRtc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.startVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioMixerBridge.startAudioMixing("https://lavaclass-cdn-sz.yyopenbuss.com/pictrues/dynamicConvert/e68cc2806adb11eda04a870992f19b96/resources/ppt/media/media1.mp3", false, false, 1);
            }
        });

        findViewById(R.id.pauseVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioMixerBridge.pauseAudioMixing();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkAndInitRtcEngine();
    }

    private void checkAndInitRtcEngine() {
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initializeEngine();
            setupVideoConfig();
            initWhiteboard();

            onCallClicked(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveChannel();
        RtcEngine.destroy();
        whiteboardView.removeAllViews();
        whiteboardView.destroy();
    }

    // rtc 回调
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // 注册 onJoinChannelSuccess 回调。
        // 本地用户成功加入频道时，会触发该回调。
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        // 注册 onFirstRemoteVideoDecoded 回调。
        // SDK 接收到第一帧远端视频并成功解码时，会触发该回调。
        // 可以在该回调中调用 setupRemoteVideo 方法设置远端视图。
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        private void setupRemoteVideo(int uid) {
            mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
            mRemoteContainer.addView(mRemoteView);
            // 设置远端视图。
            mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        }

        @Override
        // 注册 onUserOffline 回调。
        // 远端用户离开频道或掉线时，会触发该回调。
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        // 混音状态变化时的回调
        public void onAudioMixingStateChanged(int state, int reason) {
            Log.d(AudioMixerBridgeImpl.TAG, "rtcMix[RTC] onAudioMixingStateChanged state:" + state + " reason:" + reason);
            if (whiteSdk != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, reason);
            }
        }
    };

    // 初始化 RtcEngine 对象
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.rtc_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e("TAG", Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();
        // 详细设置查看 rtc 文档
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void initWhiteboard() {
        // joinRoom(ROOM_UUID, ROOM_TOKEN);
    }

    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(APP_ID, true);
        configuration.setUserCursor(true);
        configuration.setUseMultiViews(true);

        audioMixerBridge = new AudioMixerBridgeImpl(mRtcEngine, (state, code) -> {
            if (whiteSdk.getAudioMixerImplement() != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, code);
            }
        });
        WhiteSdk.setAudioMixerBridge(audioMixerBridge);
        whiteSdk = new WhiteSdk(whiteboardView, this, configuration);
        whiteSdk.setCommonCallbacks(new CommonCallback() {
            @Override
            public void onLogger(JSONObject object) {
                logAction(object.toString());
            }
        });

        /* 设置自定义全局状态，在后续回调中 GlobalState 直接进行类型转换即可 */
        WhiteDisplayerState.setCustomGlobalStateClass(GlobalState.class);

        // 如需支持用户头像，请在设置 WhiteSdkConfiguration 后，再调用 setUserPayload 方法，传入符合用户信息
        RoomParams roomParams = new RoomParams(uuid, token, DEFAULT_UID);
        roomParams.setWritable(false);

        HashMap<String, String> styleMap = new HashMap<>();
        styleMap.put("backgroundColor", "red");
        styleMap.put("top", "12px");
        styleMap.put("left", "60px");
        styleMap.put("position", "fixed");

        // String darkMode = darkModeStyle();
        WindowParams windowParams = new WindowParams()
                .setContainerSizeRatio(3f / 4)
                .setChessboard(true)
                .setDebug(true)
                // .setOverwriteStyles(darkMode)
                .setCollectorStyles(styleMap);
        // optional
        roomParams.setWindowParams(windowParams);

        whiteSdk.joinRoom(roomParams, new RoomListener() {

            @Override
            public void onPhaseChanged(RoomPhase phase) {
                logRoomInfo("onPhaseChanged: " + phase.name());
                showToast(phase.name());
            }

            @Override
            public void onDisconnectWithError(Exception e) {

            }

            @Override
            public void onKickedWithReason(String s) {

            }

            @Override
            public void onRoomStateChanged(RoomState roomState) {
                if (roomState.getWindowBoxState() != null) {
                    logRoomInfo("WindowBoxState " + roomState.getWindowBoxState());
                }
            }

            @Override
            public void onCanUndoStepsUpdate(long l) {

            }

            @Override
            public void onCanRedoStepsUpdate(long l) {

            }

            @Override
            public void onCatchErrorWhenAppendFrame(long l, Exception e) {

            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                MainRtcActivity.this.room = room;

                MemberState memberState = new MemberState();
                memberState.setCurrentApplianceName(Appliance.CLICKER);
                room.setMemberState(memberState);
            }

            @Override
            public void catchEx(SDKError sdkError) {

            }
        });
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    private void setupLocalVideo() {
        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
        joinRoom(ROOM_UUID, ROOM_TOKEN);
    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();
    }

    private void removeLocalVideo() {
        if (mLocalView != null) {
            mLocalContainer.removeView(mLocalView);
        }
        mLocalView = null;
    }

    private void removeRemoteVideo() {
        if (mRemoteView != null) {
            mRemoteContainer.removeView(mRemoteView);
        }
        mRemoteView = null;
    }

    /**
     * 加入 rtc 频道
     */
    private void joinChannel() {
        // 这里没有使用 token 加入频道，推荐使用 token 保证应用安全，详细设置参考 rtc 文档
        mRtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", 0);
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";

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
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }
}