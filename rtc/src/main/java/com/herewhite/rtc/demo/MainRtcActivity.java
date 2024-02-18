package com.herewhite.rtc.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.herewhite.rtc.demo.databinding.ActivityMainRtcBinding;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.SDKError;

import org.json.JSONObject;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class MainRtcActivity extends AppCompatActivity {
    private static final String BOARD_APP_ID = "122123/123132";
    private static final String BOARD_ROOM_UUID = "d4d4d0a073d111ee90cba7667d54b7f2";
    private static final String BOARD_ROOM_TOKEN = "NETLESSROOM_YWs9eTBJOWsxeC1IVVo4VGh0NyZub25jZT0xNjk4MzA1NTU1NzIzMDAmcm9sZT0wJnNpZz1lNTQxNGM0OWQ0MDRlYWY2NmM2NTVlNmI4ODk3MWE5OTc4YjM3MDU3YTAyN2E5ZmI4NGIwMTNjOGY4MTk3MWQ1JnV1aWQ9ZDRkNGQwYTA3M2QxMTFlZTkwY2JhNzY2N2Q1NGI3ZjI";

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };

    private ActivityMainRtcBinding binding;
    private SurfaceView localVideoView;
    private SurfaceView remoteVideoView;

    private RtcEngine rtcEngine;

    private WhiteboardView whiteboardView;
    private WhiteSdk whiteSdk;
    private AgoraAudioMixerBridge audioMixerBridge;
    private AgoraAudioEffectBridge audioEffectBridge;

    private boolean calling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRtcBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WebView.setWebContentsDebuggingEnabled(true);

        whiteboardView = binding.whiteboardView;
        binding.exit.setOnClickListener(v -> finish());
        binding.btnCall.setOnClickListener(v -> {
            setCalling(!calling);
        });

        binding.playEffect.setOnClickListener(v -> {
            int code1 = rtcEngine.getAudioEffectManager().playEffect(1, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4", 1, 1, 0.0, 100, false, 0);
            int code2 = rtcEngine.getAudioEffectManager().playEffect(2, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%201.mp3", 0, 1, 0.0, 100, false, 0);
            int code3 = rtcEngine.getAudioEffectManager().playEffect(3, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%202.mp3", 0, 1, 0.0, 100, false, 0);

            Log.d("AudioEffect", "code1: " + code1 + " code2: " + code2 + " code3: " + code3);
        });

        binding.preloadEffect.setOnClickListener(v -> {
            int code0 = rtcEngine.getAudioEffectManager().preloadEffect(1, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4");
            int code1 = rtcEngine.getAudioEffectManager().preloadEffect(2, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%201.mp3");
            int code2 = rtcEngine.getAudioEffectManager().preloadEffect(3, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%202.mp3");
            Log.e("AudioEffect", "preloadEffect code0: " + code0 + " code1: " + code1 + " code2: " + code2);

            rtcEngine.getAudioEffectManager().setEffectsVolume(50.0);
        });

        binding.pauseEffect.setOnClickListener(v -> {
            Log.d("AudioEffect", "pauseAllEffects code: " + audioEffectBridge.pauseAllEffects());
        });

        binding.resumeEffect.setOnClickListener(v -> {
            Log.d("AudioEffect", "resumeAllEffects code: " + audioEffectBridge.resumeAllEffects());
        });

        binding.getEffectCurrentPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("AudioEffect", "getEffectCurrentPosition :" + audioEffectBridge.getEffectCurrentPosition(1));
            }
        });

        binding.startAudioMixing.setOnClickListener(v -> {
            audioMixerBridge.startAudioMixing("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4", false, false, 1);
        });

        // 如果用户需要用到 rtc 混音功能来解决回声和声音抑制问题，那么必须要在 whiteSDK 之前初始化 rtcEngine
        checkAndInitRtcEngine();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkAndInitRtcEngine();
    }

    private void checkAndInitRtcEngine() {
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeEngine();
            setupVideoConfig();
            setCalling(true);
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
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
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

        @Override
        public void onUserJoined(int uid, int elapsed) {
            Log.i("agora", "User joined, uid: " + (uid & 0xFFFFFFFFL));
            super.onUserJoined(uid, elapsed);
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));
        }

        @Override
        // 混音状态变化时的回调
        public void onAudioMixingStateChanged(int state, int reason) {
            Log.d(AgoraAudioMixerBridge.TAG, "rtcMix[RTC] onAudioMixingStateChanged " + readableState(state) + ":" + readableReason(reason));
            if (whiteSdk != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, reason);
            }
        }

        @Override
        public void onAudioEffectFinished(int soundId) {
            Log.d("AudioEffect", "rtcMix[RTC] onAudioEffectFinished " + soundId);
            if (whiteSdk != null) {
                whiteSdk.getAudioEffectImplement().setEffectFinished(soundId);
            }
        }
    };

    // 初始化 RtcEngine 对象
    private void initializeEngine() {
        try {
            rtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.rtc_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e("TAG", Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        rtcEngine.enableVideo();
        rtcEngine.enableAudio();
        // 详细设置查看 rtc 文档
        rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void joinRoom(String uuid, String token) {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(BOARD_APP_ID, true);
        configuration.setUserCursor(true);
        configuration.setUseMultiViews(true);

        audioMixerBridge = new AgoraAudioMixerBridge(rtcEngine, (state, code) -> {
            if (whiteSdk.getAudioMixerImplement() != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, code);
            }
        });
        audioEffectBridge = new AgoraAudioEffectBridge(rtcEngine);
        whiteSdk = new WhiteSdk.Builder(whiteboardView, configuration)
                // .setAudioMixerBridge(audioMixerBridge)
                .setAudioEffectBridge(audioEffectBridge)
                .setCommonCallback(new CommonCallback() {
                    @Override
                    public void onLogger(JSONObject object) {
                        Log.i("ROOM_LOGGER", object.toString());
                    }
                })
                .build();

        RoomParams roomParams = new RoomParams(uuid, token, Utils.getUserId(this));
        roomParams.setWritable(true);

        whiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {

            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
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
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    private void setupRemoteVideo(int uid) {
        remoteVideoView = RtcEngine.CreateRendererView(getBaseContext());
        binding.remoteVideoViewContainer.addView(remoteVideoView);
        rtcEngine.setupRemoteVideo(new VideoCanvas(remoteVideoView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void setupLocalVideo() {
        localVideoView = RtcEngine.CreateRendererView(getBaseContext());
        localVideoView.setZOrderMediaOverlay(true);
        binding.localVideoViewContainer.addView(localVideoView);
        rtcEngine.setupLocalVideo(new VideoCanvas(localVideoView, VideoCanvas.RENDER_MODE_HIDDEN, getRtcUid()));
    }

    public void setCalling(boolean calling) {
        if (calling) {
            startCall();
        } else {
            endCall();
        }
        this.calling = calling;
        binding.btnCall.setSelected(calling);
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
        joinRoom(BOARD_ROOM_UUID, BOARD_ROOM_TOKEN);
    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();
    }

    private void removeLocalVideo() {
        if (localVideoView != null) {
            binding.localVideoViewContainer.removeView(localVideoView);
            localVideoView = null;
        }
    }

    private void removeRemoteVideo() {
        if (remoteVideoView != null) {
            binding.remoteVideoViewContainer.removeView(remoteVideoView);
            remoteVideoView = null;
        }
    }

    private void joinChannel() {
        // 这里没有使用 token 加入频道，推荐使用 token 保证应用安全，详细设置参考 rtc 文档
        rtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", getRtcUid());
    }

    private void leaveChannel() {
        rtcEngine.leaveChannel();
    }

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }


    private int getRtcUid() {
        return Utils.getFallbackRtcId(this);
    }

    public String readableState(int state) {
        switch (state) {
            case Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY:
                return "PLAYING";
            case Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_PAUSED:
                return "PAUSED";
            case Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_STOPPED:
                return "STOPPED";
            case Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR:
                return "FAILED";
            default:
                return state + "";
        }
    }

    public String readableReason(int reason) {
        switch (reason) {
            case Constants.ERR_OK:
                return "OK";
            case Constants.AUDIO_MIXING_REASON_CAN_NOT_OPEN:
                return "CAN_NOT_OPEN";
            case Constants.AUDIO_MIXING_REASON_TOO_FREQUENT_CALL:
                return "TOO_FREQUENT_CALL";
            case Constants.AUDIO_MIXING_REASON_INTERRUPTED_EOF:
                return "INTERRUPTED_EOF";
            case Constants.AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED:
                return "ONE_LOOP_COMPLETED";
            case Constants.AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED:
                return "ALL_LOOPS_COMPLETED";
            case Constants.AUDIO_MIXING_REASON_STOPPED_BY_USER:
                return "STOPPED_BY_USER";
            default:
                return reason + "";
        }
    }
}