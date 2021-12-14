package com.herewhite.demo.test;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.herewhite.demo.BaseActivity;
import com.herewhite.demo.common.DemoAPI;
import com.herewhite.demo.R;
import com.herewhite.demo.utils.EmptyRoomListener;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RectangleConfig;
import com.herewhite.sdk.domain.SDKError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CameraMoveImageActivity extends BaseActivity {
    public static final String TEST_IMAGE_URL = "https://placekitten.com/1920/1080";

    private DemoAPI demoAPI = DemoAPI.get();

    private WhiteboardView whiteboardView;
    private ImageView imageView;
    private WhiteSdk whiteSdk;
    private Room room;

    private double imgH;
    private double imgW;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_move_image);
        getSupportActionBar().hide();

        WhiteboardView.setWebContentsDebuggingEnabled(true);
        whiteboardView = findViewById(R.id.white);
        whiteboardView.setBackgroundColor(Color.TRANSPARENT);

        imageView = findViewById(R.id.imageView);

        joinRoom();
    }

    private void enableButton() {
        findViewById(R.id.insertImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage(TEST_IMAGE_URL, imageView);
            }
        });

        findViewById(R.id.scaleSmall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToSmall();
            }
        });

        findViewById(R.id.scaleCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCamera();
            }
        });

        findViewById(R.id.scaleCameraFill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCameraFillContent();
            }
        });

        findViewById(R.id.orientation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientation();
            }
        });
    }

    private void joinRoom() {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(demoAPI.getAppId(), true);
        whiteSdk = new WhiteSdk(whiteboardView, this, configuration);

        RoomParams roomParams = new RoomParams(demoAPI.getRoomUUID(), demoAPI.getRoomToken(), DemoAPI.DEFAULT_UID);
        whiteSdk.joinRoom(roomParams, new EmptyRoomListener(), new Promise<Room>() {
            @Override
            public void then(Room room) {
                CameraMoveImageActivity.this.room = room;
                loadImageInfo(TEST_IMAGE_URL);
            }

            @Override
            public void catchEx(SDKError t) {
                showToast(t.getMessage());
            }
        });
    }

    /**
     * This code is used as an example, the application needs to manage io and async itself.
     * The application may get the image width and height from the api
     *
     * @param src
     */
    public void loadImageInfo(String src) {
        new Thread(() -> {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                InputStream input = new URL(src).openStream();
                BitmapFactory.decodeStream(input, null, options);
                imgH = options.outHeight;
                imgW = options.outWidth;

                runOnUiThread(() -> enableButton());
            } catch (IOException e) {
            }
        }).start();
    }

    public void loadImage(String src, ImageView target) {
        new Thread(() -> {
            try {
                InputStream input = new URL(src).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                runOnUiThread(() -> target.setImageBitmap(bitmap));
            } catch (IOException ignored) {
            }
        }).start();
    }

    public void moveToSmall() {
        CameraConfig config = new CameraConfig();
        config.setScale(0.1);
        room.moveCamera(config);
    }

    public void moveCamera() {
        RectangleConfig config = new RectangleConfig(imgW, imgH);
        room.moveCameraToContainer(config);

        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    public void moveCameraFillContent() {
        double height = whiteboardView.getHeight();
        double width = whiteboardView.getWidth();

        double factor = Math.min((imgW / imgH) / (width / height), (imgH / imgW) / (height / width));
        room.moveCameraToContainer(new RectangleConfig(imgW * factor, imgH * factor));

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    public void orientation() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (room != null) {
            room.disconnect();
        }
    }
}
