
package com.herewhite.demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.google.gson.Gson;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.utils.MapBuilder;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.Converter;
import com.herewhite.sdk.ConverterCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomCallbacks;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.converter.ConvertType;
import com.herewhite.sdk.converter.ConverterV5;
import com.herewhite.sdk.converter.ImageFormat;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.ContentModeConfig;
import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.FontFace;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Point;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RectangleConfig;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.ViewMode;
import com.herewhite.sdk.domain.WhiteDisplayerState;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class RoomActivity extends BaseActivity {
    static final String TAG = RoomActivity.class.getSimpleName();

    /**
     * ??? iOS ????????????
     */
    private static final String EVENT_NAME = "WhiteCommandCustomEvent";
    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";
    final String SCENE_DIR = "/dir";

    final Gson gson = new Gson();
    final DemoAPI demoAPI = DemoAPI.get();

    // Room Params
    private String uuid;
    private String token;

    WhiteboardView mWhiteboardView;
    @VisibleForTesting
    WhiteSdk mWhiteSdk;
    @VisibleForTesting
    Room mRoom;
    @VisibleForTesting
    RoomCallbacks mRoomCallbackHock = new AbstractRoomCallbacks() {
    };

    /**
     * ????????? GlobalState ??????
     * ????????? GlobalState ???????????????????????? {@link WhiteDisplayerState#setCustomGlobalStateClass(Class)}
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
        setContentView(R.layout.activity_room);

        mWhiteboardView = findViewById(R.id.white);
        mWhiteboardView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        // ?????????????????? HttpDns????????? DNS ???????????????
        useHttpDnsService(false);

        // ?????? LocalFileWebViewClient ??? ?????? ppt ?????????????????????????????????????????????????????????????????????????????????
        LocalFileWebViewClient client = new LocalFileWebViewClient();
        client.setPptDirectory(getCacheDir().getAbsolutePath());
        mWhiteboardView.setWebViewClient(client);

        // ????????????
        testMarkIdling(false);
        setupRoom();

        findViewById(R.id.sendSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RectangleConfig config = new RectangleConfig(200d, 400d);
                mRoom.moveCameraToContainer(config);
            }
        });
    }

    private void setupRoom() {
        String uuid = getIntent().getStringExtra(StartActivity.EXTRA_ROOM_UUID);

        DemoAPI.Result result = new DemoAPI.Result() {
            @Override
            public void success(String uuid, String token) {
                joinRoom(uuid, token);
            }

            @Override
            public void fail(String message) {
                alert("??????????????????", message);
            }
        };

        if (uuid != null) {
            demoAPI.getRoomToken(uuid, result);
        } else {
            demoAPI.getNewRoom(result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //region room
    private void joinRoom(String uuid, String token) {
        logRoomInfo("room uuid: " + uuid + "\nroom token: " + token);

        //???????????????????????????
        this.uuid = uuid;
        this.token = token;

        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        /*??????????????????*/
        configuration.setUserCursor(true);
        //?????? ppt ????????????????????????????????????????????????????????????
        configuration.setFonts(new MapBuilder<String, String>().put("??????", "https://your-cdn.com/Songti.ttf").build());

        mWhiteSdk = new WhiteSdk(mWhiteboardView, this, configuration);

        //???????????? API???????????? whiteSDKConfig ??????????????? setHasUrlInterrupterAPI??????????????????????????????????????????
        mWhiteSdk.setCommonCallbacks(new CommonCallback() {
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

            @Override
            public void onLogger(JSONObject object) {
                logAction(object.toString());
            }
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

        /** ???????????????????????????????????????????????? GlobalState ?????????????????????????????? */
        WhiteDisplayerState.setCustomGlobalStateClass(MyGlobalState.class);

        //??????????????????????????????????????? WhiteSdkConfiguration ??????????????? setUserPayload ?????????????????????????????????
        RoomParams roomParams = new RoomParams(uuid, token, DemoAPI.DEFAULT_UID);
        roomParams.setDisableNewPencil(false);
        roomParams.setWritable(true);

        final Date joinDate = new Date();
        logRoomInfo("native join " + joinDate);
        mWhiteSdk.joinRoom(roomParams, new RoomCallbacks() {
            @Override
            public void onCanUndoStepsUpdate(long canUndoSteps) {
                mRoomCallbackHock.onCanUndoStepsUpdate(canUndoSteps);
                logRoomInfo("canUndoSteps: " + canUndoSteps);
            }

            @Override
            public void onCanRedoStepsUpdate(long canRedoSteps) {
                mRoomCallbackHock.onCanRedoStepsUpdate(canRedoSteps);
                logRoomInfo("onCanRedoStepsUpdate: " + canRedoSteps);
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
                mRoomCallbackHock.onCatchErrorWhenAppendFrame(userId, error);
                logRoomInfo("onCatchErrorWhenAppendFrame: " + userId + " error " + error.getMessage());
            }

            @Override
            public void onPhaseChanged(RoomPhase phase) {
                mRoomCallbackHock.onPhaseChanged(phase);
                //?????????????????????????????????????????????
                logRoomInfo("onPhaseChanged: " + phase.name());
                showToast(phase.name());
            }

            @Override
            public void onDisconnectWithError(Exception e) {
                mRoomCallbackHock.onDisconnectWithError(e);
                logRoomInfo("onDisconnectWithError: " + e.getMessage());
            }

            @Override
            public void onKickedWithReason(String reason) {
                mRoomCallbackHock.onKickedWithReason(reason);
                logRoomInfo("onKickedWithReason: " + reason);
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                mRoomCallbackHock.onRoomStateChanged(modifyState);
                logRoomInfo("onRoomStateChanged:" + gson.toJson(modifyState));
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                //?????????????????????????????????
                logRoomInfo("native join in room duration: " + (System.currentTimeMillis() - joinDate.getTime()) / 1000f + "s");
                mRoom = room;
                addCustomEventListener();

                testMarkIdling(true);
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
            AlertDialog alertDialog = new AlertDialog.Builder(RoomActivity.this).create();
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
            /** ??????????????? id ?????????sdk ?????????????????? HttpDns ?????????????????? */
            HttpDnsService httpDnsService = HttpDns.getService(getApplicationContext(), "188301");
            httpDnsService.setPreResolveHosts(new ArrayList<>(Arrays.asList("expresscloudharestoragev2.herewhite.com", "cloudharev2.herewhite.com", "scdncloudharestoragev3.herewhite.com", "cloudcapiv4.herewhite.com")));
            mWhiteboardView.setWebViewClient(new WhiteWebViewClient(httpDnsService));
        }
    }

    private void addCustomEventListener() {
        mRoom.addMagixEventListener(EVENT_NAME, event -> {
            logRoomInfo("customEvent payload: " + event.getPayload().toString());
            showToast(gson.toJson(event.getPayload()));
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Note???sdk??????????????????size?????????
        // ????????????????????????????????????????????????WhiteboardView.setAutoResize(false)??????????????????; ?????????????????????????????????Room.refreshViewSize()
        // logRoomInfo("width:" + mWhiteboardView.getWidth() / getResources().getDisplayMetrics().density + " height: " + mWhiteboardView.getHeight() / getResources().getDisplayMetrics().density);
        // onConfigurationChanged ???????????????????????????????????????????????????????????????
        // new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
        //     @Override
        //     public void run() {
        //         mRoom.refreshViewSize();
        //         logRoomInfo("width:" + mWhiteboardView.getWidth() / getResources().getDisplayMetrics().density + " height: " + mWhiteboardView.getHeight() / getResources().getDisplayMetrics().density);
        //     }
        // }, 1000);
    }

    //endregion

    //region menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    private CameraBound customBound(double maxScale) {
        CameraBound bound = new CameraBound();
        bound.setCenterX(0d);
        bound.setCenterY(0d);
        bound.setHeight((double) (mWhiteboardView.getHeight() / this.getResources().getDisplayMetrics().density));
        bound.setWidth((double) (mWhiteboardView.getWidth() / this.getResources().getDisplayMetrics().density));
        ContentModeConfig contentModeConfig = new ContentModeConfig();
        contentModeConfig.setScale(maxScale);
        contentModeConfig.setMode(ContentModeConfig.ScaleMode.CENTER_INSIDE_SCALE);
        bound.setMaxContentMode(contentModeConfig);
        return bound;
    }

    public void scalePptToFit(MenuItem item) {
        mRoom.scalePptToFit(AnimationMode.Continuous);
    }

    public void reconnect(MenuItem item) {
        testMarkIdling(false);
        mRoom.disconnect(new Promise<Object>() {
            @Override
            public void then(Object b) {
                joinRoom(RoomActivity.this.uuid, RoomActivity.this.token);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void setWritableFalse(MenuItem item) {
        mRoom.setWritable(false, new Promise<Boolean>() {
            @Override
            public void then(Boolean aBoolean) {
                logRoomInfo("room writable: " + aBoolean);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void setWritableTrue(MenuItem item) {
        mRoom.setWritable(true, new Promise<Boolean>() {
            @Override
            public void then(Boolean aBoolean) {
                logRoomInfo("room writable: " + aBoolean);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void orientation(MenuItem item) {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            RoomActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            RoomActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public void setBound(MenuItem item) {
        CameraBound bound = customBound(3);
        mRoom.setCameraBound(bound);
    }

    public void nextScene(MenuItem item) {
        int nextIndex = mRoom.getSceneState().getIndex() + 1;
        mRoom.setSceneIndex(nextIndex, new Promise<Boolean>() {
            @Override
            public void then(Boolean result) {

            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    public void undoRedoOperation(MenuItem item) {
        // ?????????????????????????????????????????? redo undo
        mRoom.disableSerialization(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRoom.undo();
            }
        }, 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRoom.redo();
            }
        }, 1500);
    }

    public void duplicate(MenuItem item) {
        mRoom.duplicate();
    }

    public void copyPaste(MenuItem item) {
        mRoom.copy();
        mRoom.paste();
    }

    public void deleteOperation(MenuItem item) {
        mRoom.deleteOperation();
    }

    public void getPreviewImage(MenuItem item) {
        mRoom.getScenePreviewImage("/init", new Promise<Bitmap>() {
            @Override
            public void then(Bitmap bitmap) {
                logAction("get bitmap");
            }

            @Override
            public void catchEx(SDKError t) {
                logAction("get bitmap error");
            }
        });
    }

    public void getSceneImage(MenuItem item) {
        mRoom.getSceneSnapshotImage("/init", new Promise<Bitmap>() {
            @Override
            public void then(Bitmap bitmap) {
                logAction("get bitmap");
            }

            @Override
            public void catchEx(SDKError t) {
                logAction("get bitmap error");
            }
        });
    }

    public void staticConvert(MenuItem item) {
        Converter c = new Converter(this.token);
        c.startConvertTask("https://white-cn-edge-doc-convert.oss-cn-hangzhou.aliyuncs.com/LightWaves.pdf", Converter.ConvertType.Static, new ConverterCallbacks() {
            @Override
            public void onFailure(ConvertException e) {
                logAction(e.getMessage());
            }

            @Override
            public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {
                mRoom.putScenes("/static", ppt.getScenes(), 0);
                mRoom.setScenePath("/static/1");
                logAction(convertInfo.toString());
            }

            @Override
            public void onProgress(Double progress, ConversionInfo convertInfo) {
                logAction(String.valueOf(progress));
            }
        });

        // ConvertV5
        ConverterV5.Builder builder = new ConverterV5.Builder();
        ConverterV5 converter = builder
                .setResource("https://white-cn-edge-doc-convert.oss-cn-hangzhou.aliyuncs.com/LightWaves.pdf")
                .setType(ConvertType.Static)
                .setScale(1.5)
                .setOutputFormat(ImageFormat.JPEG)
                .setSdkToken(demoAPI.getSdkToken())
                .setTaskUuid(null)
                .setTaskToken(null)
                .setCallback(new ConverterCallbacks() {
                    @Override
                    public void onProgress(Double progress, ConversionInfo convertInfo) {

                    }

                    @Override
                    public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {

                    }

                    @Override
                    public void onFailure(ConvertException e) {

                    }
                })
                .build();
        converter.startConvertTask();
    }

    public void dynamicConvert(MenuItem item) {
        Converter c = new Converter(this.token);
        c.startConvertTask("https://white-cn-edge-doc-convert.oss-cn-hangzhou.aliyuncs.com/-1/1.pptx", Converter.ConvertType.Dynamic, new ConverterCallbacks() {
            @Override
            public void onFailure(ConvertException e) {
                logAction(e.getMessage());
            }

            @Override
            public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {
                mRoom.putScenes("/dynamic", ppt.getScenes(), 0);
                mRoom.setScenePath("/dynamic/1");
                logAction(convertInfo.toString());
            }

            @Override
            public void onProgress(Double progress, ConversionInfo convertInfo) {
                logAction(String.valueOf(progress));
            }
        });

        // ConvertV5
        ConverterV5.Builder builder = new ConverterV5.Builder();
        ConverterV5 converter = builder
                .setResource("https://white-cn-edge-doc-convert.oss-cn-hangzhou.aliyuncs.com/-1/1.pptx")
                .setType(ConvertType.Dynamic)
                .setPreview(true)
                .setSdkToken(demoAPI.getSdkToken())
                .setTaskUuid(null)
                .setTaskToken(null)
                .setCallback(new ConverterCallbacks() {
                    @Override
                    public void onProgress(Double progress, ConversionInfo convertInfo) {

                    }

                    @Override
                    public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {

                    }

                    @Override
                    public void onFailure(ConvertException e) {

                    }
                })
                .build();
        converter.startConvertTask();
    }

    public void broadcast(MenuItem item) {
        logAction();
        mRoom.setViewMode(ViewMode.Broadcaster);
    }

    public void getBroadcastState(MenuItem item) {
        logAction();
        BroadcastState broadcastState = mRoom.getBroadcastState();
        showToast(broadcastState.getMode());
        logRoomInfo(gson.toJson(broadcastState));
    }

    public void moveCamera(MenuItem item) {
        logAction();
        CameraConfig config = new CameraConfig();
        config.setCenterX(100d);
        mRoom.moveCamera(config);
    }

    public void moveRectangle(MenuItem item) {
        logAction();
        RectangleConfig config = new RectangleConfig(200d, 400d);
        mRoom.moveCameraToContainer(config);
    }

    public void dispatchCustomEvent(MenuItem item) {
        logAction();
        HashMap<String, String> payload = new HashMap<>();
        payload.put("device", "android");

        mRoom.dispatchMagixEvent(new AkkoEvent(EVENT_NAME, payload));
    }

    public void cleanScene(MenuItem item) {
        logAction();
        mRoom.cleanScene(true);
    }

    public void insertNewScene(MenuItem item) {
        logAction();
        mRoom.putScenes(SCENE_DIR, new Scene[]{
                new Scene("page1")}, 0);
        mRoom.setScenePath(SCENE_DIR + "/page1");
    }

    public void insertPPT(MenuItem item) {
        logAction();
        mRoom.putScenes(SCENE_DIR, new Scene[]{
                new Scene("page2", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 600d, 600d))
        }, 0);
        mRoom.setScenePath(SCENE_DIR + "/page2");
    }

    public void insertImage(MenuItem item) {
        mRoom.insertImage(new ImageInformationWithUrl(0d, 0d, 100d, 200d, "https://white-pan.oss-cn-shanghai.aliyuncs.com/40/image/mask.jpg"));
    }

    public void getScene(MenuItem item) {
        logAction();
        logAction(gson.toJson(mRoom.getScenes()));
    }

    public void getRoomPhase(MenuItem item) {
        logAction();
        logRoomInfo("RoomPhase: " + gson.toJson(mRoom.getRoomPhase()));
    }

    public void getRoomState(MenuItem item) {
        logAction();
        //???????????????????????????????????????
        logRoomInfo("roomState: " + gson.toJson(mRoom.getRoomState()));
    }

    public void disconnect(MenuItem item) {

        //???????????????????????????????????????
        mRoom.disconnect(new Promise<Object>() {
            @Override
            public void then(Object o) {
                logAction("disconnect success");
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });

        //???????????????????????????????????????????????????
        //room.disconnect();
    }

    public void disableOperation(MenuItem item) {
        logAction();
        mRoom.disableOperations(true);
    }

    public void cancelDisableOperation(MenuItem item) {
        logAction();
        mRoom.disableOperations(false);
    }

    public void textArea(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{99, 99, 99});
        memberState.setCurrentApplianceName(Appliance.TEXT);
        memberState.setStrokeWidth(10);
        memberState.setTextSize(10);
        mRoom.setMemberState(memberState);
    }

    public void selector(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setCurrentApplianceName(Appliance.SELECTOR);
        mRoom.setMemberState(memberState);
    }

    public void pencil(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{99, 99, 99});
        memberState.setCurrentApplianceName(Appliance.PENCIL);
        memberState.setStrokeWidth(10);
        memberState.setTextSize(10);
        mRoom.setMemberState(memberState);
    }

    public void rectangle(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{99, 99, 99});
        memberState.setCurrentApplianceName(Appliance.RECTANGLE);
        memberState.setStrokeWidth(10);
        memberState.setTextSize(10);
        mRoom.setMemberState(memberState);
    }

    public void color(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{200, 200, 200});
        memberState.setCurrentApplianceName(Appliance.PENCIL);
        memberState.setStrokeWidth(4);
        memberState.setTextSize(10);
        mRoom.setMemberState(memberState);
    }

    public void shape(MenuItem item) {
        logAction();
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{200, 200, 200});
        // defalut Shape Triangle
        memberState.setCurrentApplianceName(Appliance.SHAPE);
        // memberState.setShapeType(ShapeType.Rhombus);
        memberState.setStrokeWidth(4);
        memberState.setTextSize(10);
        mRoom.setMemberState(memberState);
    }

    public void convertPoint(MenuItem item) {
        //?????????????????????????????????????????????
        mRoom.convertToPointInWorld(0, 0, new Promise<Point>() {
            @Override
            public void then(Point point) {
                logRoomInfo(gson.toJson(point));
            }

            @Override
            public void catchEx(SDKError t) {
                Log.e(TAG, "convertToPointInWorld error " + t.getStackTrace().toString());
            }
        });
    }

    public void externalEvent(MenuItem item) {
        logAction();
    }

    public void zoomChange(MenuItem item) {
        CameraConfig cameraConfig = new CameraConfig();
        cameraConfig.setAnimationMode(AnimationMode.Immediately);

        double scale = 1d;
        if (mRoom.getRoomState().getCameraState() != null) {
            scale = mRoom.getRoomState().getCameraState().getScale();
        }
        if (scale != 1) {
            cameraConfig.setScale(1d);
        } else {
            cameraConfig.setScale(5d);
        }
        mRoom.moveCamera(cameraConfig);
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
}
