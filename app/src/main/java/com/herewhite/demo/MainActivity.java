package com.herewhite.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteBroadView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.DeviceType;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;

public class MainActivity extends AppCompatActivity {

    WhiteBroadView whiteBroadView;
    Gson gson = new Gson();

    public <T extends View> T getView(int viewId) {
        View view = findViewById(viewId);
        return (T) view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.js);

        whiteBroadView = getView(R.id.white);
        // /?uuid=test&roomToken=123&viewWidth=0&viewHeight=0  调用 native 的 createRoom 后得到

        WhiteSdk whiteSdk = new WhiteSdk(whiteBroadView, this, new WhiteSdkConfiguration(DeviceType.touch, 10, 0.1));
        whiteSdk.addRoomCallbacks(new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
                // handle room phase
            }

            @Override
            public void onRoomStateChanged(RoomState modifyState) {
                showToast(gson.toJson(modifyState));
            }
        });
        whiteSdk.joinRoom(new RoomParams("test", "123"), new Promise<Room>() {
            @Override
            public void then(Room room) {
                GlobalState globalState = new GlobalState();
                globalState.setCurrentSceneIndex(2);
                room.setGlobalState(globalState);

//                room.setViewSize(100, 100);

            }

            @Override
            public void catchEx(Throwable t) {

            }
        });

    }

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }


}
