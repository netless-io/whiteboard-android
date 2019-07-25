package com.herewhite.sdk;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.Base64;
import com.google.gson.Gson;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RectangleConfig;
import com.herewhite.sdk.domain.SDKError;

import wendu.dsbridge.OnReturnValue;

public class Displayer {

    @ColorInt
    private int backgroudColor = Color.WHITE;
    protected final WhiteBroadView bridge;
    protected String uuid;
    protected final Context context;
    protected WhiteSdk sdk;
    protected final static Gson gson = new Gson();

    public Displayer(String uuid, WhiteBroadView bridge, Context context, WhiteSdk sdk) {
        this.uuid = uuid;
        this.bridge = bridge;
        this.context = context;
        this.sdk = sdk;
    }

    public void setBackgroudColor(int intColor) {
        if ((intColor & 0xFF000000) == 0xFF000000) {
            this.bridge.callHandler("displayer.background", new Object[]{Displayer.toHexString(intColor)});
            backgroudColor = intColor;
        } else {
            throw new AssertionError("alpha is not support to change");
        }
    }

    public int getBackgroudColor() {
        return backgroudColor;
    }

    private final static String toHexString(int intColor) {
        return String.format("#%06X", (0xFFFFFF & intColor));
    }

    public void getScenePreviewImage(String scenePath, final Promise<Bitmap>promise) {
        this.bridge.callHandler("displayerAsync.scenePreview", new Object[]{scenePath}, new OnReturnValue<String>() {
            @Override
            public void onValue(String retValue) {
                Bitmap bitmap = null;
                try {
                    bitmap = transformBase64toBitmap(retValue);
                } catch (Exception e) {
                    promise.catchEx(new SDKError(e.getMessage()));
                }
                if (bitmap != null) {
                    promise.then(bitmap);
                }
            }
        });
    }

    public void getSceneSnapshotImage(String scenePath, final Promise<Bitmap>promise) {
        this.bridge.callHandler("displayerAsync.sceneSnapshot", new Object[]{scenePath}, new OnReturnValue<String>() {
            @Override
            public void onValue(String retValue) {
                Bitmap bitmap = null;
                try {
                    bitmap = transformBase64toBitmap(retValue);
                } catch (Exception e) {
                    promise.catchEx(new SDKError(e.getMessage()));
                }
                if (bitmap != null) {
                    promise.then(bitmap);
                }
            }
        });
    }

    private Bitmap transformBase64toBitmap(String base64String) {
        final String pureBase64Encoded = base64String.substring(base64String.indexOf(",")  + 1);
        final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = context.getResources().getDisplayMetrics().densityDpi;
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, opts);
    }

    public void moveCamera(CameraConfig camera) {
        this.bridge.callHandler("displayer.moveCamera", new Object[]{camera});
    }

    public void moveCameraToContainer(RectangleConfig rectange) {
        this.bridge.callHandler("displayer.moveCameraToContain", new Object[]{rectange});
    }
}
