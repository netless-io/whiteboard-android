package com.herewhite.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.AbstractPlayerEventListener;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Environment;
import com.herewhite.sdk.Logger;
import com.herewhite.sdk.domain.MemberInformation;
import com.herewhite.sdk.domain.Point;
import com.herewhite.sdk.Player;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.UpdateCursor;
import com.herewhite.sdk.domain.UrlInterrupter;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.herewhite.demo.DemoAPI.TEST_ROOM_TOKEN;
import static com.herewhite.demo.DemoAPI.TEST_UUID;

public class MainActivity extends AppCompatActivity {

    WhiteBroadView whiteBroadView;
    Gson gson = new Gson();
    DemoAPI demoAPI = new DemoAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.js);
        whiteBroadView = (WhiteBroadView) findViewById(R.id.white);
//
        try {
            realtime();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        player();


    }

    private void player() {
        WhiteSdk whiteSdk = new WhiteSdk(
                whiteBroadView,
                MainActivity.this,
                new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1, true),
                new UrlInterrupter() {
                    @Override
                    public String urlInterrupter(String sourceUrl) {
                        return sourceUrl;
                    }
                });

        PlayerConfiguration playerConfiguration = new PlayerConfiguration();
        playerConfiguration.setRoom("f892bd37ba6c4031a8e59b52d308f829");
        playerConfiguration.setAudioUrl("https://ohuuyffq2.qnssl.com/98398e2c5a43d74321214984294c157e_60def9bac25e4a378235f6249cae63c1.m3u8");

        whiteSdk.createPlayer(playerConfiguration, new AbstractPlayerEventListener() {
            @Override
            public void onPhaseChanged(PlayerPhase phase) {
                showToast(gson.toJson(phase));
            }

            @Override
            public void onLoadFirstFrame() {
                showToast("onLoadFirstFrame");
            }

            @Override
            public void onSliceChanged(String slice) {
                showToast(slice);
            }

            @Override
            public void onPlayerStateChanged(PlayerState modifyState) {
                showToast(gson.toJson(modifyState));
            }

            @Override
            public void onStoppedWithError(SDKError error) {
                showToast(error.getJsStack());
            }

            @Override
            public void onScheduleTimeChanged(long time) {
                showToast(time);
            }

            @Override
            public void onCatchErrorWhenAppendFrame(SDKError error) {
                showToast(error.getJsStack());
            }

            @Override
            public void onCatchErrorWhenRender(SDKError error) {
                showToast(error.getJsStack());
            }

            @Override
            public void onCursorViewsUpdate(UpdateCursor updateCursor) {
                showToast(gson.toJson(updateCursor));
            }
        }, new Promise<Player>() {
            @Override
            public void then(Player player) {
                player.play();
            }

            @Override
            public void catchEx(SDKError t) {
                Logger.error("create player error, ", t);
            }
        });
    }

    private void realtime() throws IOException {
        demoAPI.createRoom("unknow", 100, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JsonObject room = gson.fromJson(response.body().string(), JsonObject.class);
                String uuid = room.getAsJsonObject("msg").getAsJsonObject("room").get("uuid").getAsString();
                String roomToken = room.getAsJsonObject("msg").get("roomToken").getAsString();
                Log.i("white", uuid + "|" + roomToken);
                if (whiteBroadView.getEnv() == Environment.dev) {
                    joinRoom(TEST_UUID, TEST_ROOM_TOKEN);
                } else {
                    joinRoom(uuid, roomToken);
                }

            }
        });
    }

    private void joinRoom(String uuid, String roomToken) {
        WhiteSdkConfiguration sdkConfiguration = new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1, true);
        sdkConfiguration.setUserCursor(true);
        WhiteSdk whiteSdk = new WhiteSdk(
                whiteBroadView,
                MainActivity.this,
                sdkConfiguration,
                new UrlInterrupter() {
                    @Override
                    public String urlInterrupter(String sourceUrl) {
                        return sourceUrl;
                    }
                });
        MemberInformation info = new MemberInformation("313131");
        info.setAvatar("https://white-pan.oss-cn-shanghai.aliyuncs.com/40/image/mask.jpg");
        Log.i("room message:", uuid + "\n" + roomToken);
        whiteSdk.joinRoom(new RoomParams(uuid, roomToken), new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
                // handle room phase
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                showToast(gson.toJson(modifyState));
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {


//                room.insertNewPage(1);
//                GlobalState globalState = new GlobalState();
//                globalState.setCurrentSceneIndex(0);
//                room.setGlobalState(globalState);
//                MemberState memberState = new MemberState();
//                memberState.setStrokeColor(new int[]{99, 99, 99});
//                memberState.setCurrentApplianceName(Appliance.RECTANGLE);
//                memberState.setStrokeWidth(10);
//                memberState.setTextSize(10);
//
//////                memberState.setStrokeWidth(10);
//                room.setMemberState(memberState);
//
//                room.insertImage(new ImageInformationWithUrl(0d, 0d, 100d, 200d, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/image.png"));

//                room.zoomChange(10);
//                room.setViewMode(ViewMode.Broadcaster);
//                ScreenshotParam screenshotParam = new ScreenshotParam();
//                screenshotParam.setHeight(640);
//                screenshotParam.setWidth(480);

//                room.disableOperations(true);

//                room.screenshot(screenshotParam, new Promise<Object>() {
//                    @Override
//                    public void then(Object o) {
//                        Logger.info(o.toString());
//                    }
//
//                    @Override
//                    public void catchEx(SDKError t) {
//                        Logger.error("screenshot  error", t);
//                    }
//                });

//                room.convertToPointInWorld(10, 10, new Promise<Point>() {
//                    @Override
//                    public void then(Point point) {
//                        Logger.info(point.toString());
//                    }
//
//                    @Override
//                    public void catchEx(SDKError t) {
//                        Logger.error("convertToPointInWorld  error", t);
//                    }
//                });

//                room.addMagixEventListener("helloworld", new EventListener() {
//                    @Override
//                    public void onEvent(EventEntry eventEntry) {
//                        showToast(gson.toJson(eventEntry.getPayload()));
//                    }
//                });
//////
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Map<String, String> payload = new HashMap<>();
//                payload.put("test", "1");
//                room.dispatchMagixEvent(new AkkoEvent("helloworld", payload));
//////
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                payload = new HashMap<>();
//                payload.put("test", "2");
//                room.dispatchMagixEvent(new AkkoEvent("helloworld", payload));
//
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                room.removeMagixEventListener("helloworld");
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                payload = new HashMap<>();
//                payload.put("test", "nothing");
//
//                room.dispatchMagixEvent(new AkkoEvent("helloworld", payload));


//
//                room.insertNewPage(1);
//                room.removePage(1);
//
//                GlobalState globalState = new GlobalState();
//                globalState.setCurrentSceneIndex(1);
//                room.setGlobalState(globalState);
//
//                room.pushPptPages(new PptPage[]{
//                        new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/image.png", 600d, 600d),
//                });
//
//                GlobalState globalState = new GlobalState();
//                globalState.setCurrentSceneIndex(1);
//                room.setGlobalState(globalState);


                room.putScenes("/good", new Scene[]{
                        new Scene("page1", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/image.png", 600d, 600d))
                }, 0);

                room.setScenePath("/good/page1");

                room.moveScene("/good/page1", "/good/page2");

//                room.removeScenes("/good/page1");

//                room.externalDeviceEventDown(new RoomMouseEvent(100, 300));
//                room.externalDeviceEventMove(new RoomMouseEvent(100, 400));
//                room.externalDeviceEventMove(new RoomMouseEvent(100, 500));
//                room.externalDeviceEventMove(new RoomMouseEvent(100, 600));
//                room.externalDeviceEventMove(new RoomMouseEvent(100, 700));
//                room.externalDeviceEventUp(new RoomMouseEvent(100, 700));

//                room.setViewMode(ViewMode.broadcaster);
//
//                room.getBroadcastState(new Promise<BroadcastState>() {
//                    @Override
//                    public void then(BroadcastState broadcastState) {
//                        showToast(broadcastState.getMode());
//                    }
//
//                    @Override
//                    public void catchEx(Exception t) {
//
//                    }
//                });

            }

            @Override
            public void catchEx(SDKError t) {
                showToast(t.getMessage());
            }
        });
    }

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }


}
