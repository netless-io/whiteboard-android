package com.herewhite.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.Base64;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RectangleConfig;
import com.herewhite.sdk.domain.SDKError;

import wendu.dsbridge.OnReturnValue;

/**
 * 白板房间基类
 */
public class Displayer {

    @ColorInt
    private int backgroundColor = Color.WHITE;
    protected final WhiteboardView bridge;
    protected String uuid;
    protected final Context context;
    protected WhiteSdk sdk;
    protected final static Gson gson = new Gson();

    public Displayer(String uuid, WhiteboardView bridge, Context context, WhiteSdk sdk) {
        this.uuid = uuid;
        this.bridge = bridge;
        this.context = context;
        this.sdk = sdk;
    }

    /**
     * 锁定视野范围
     *
     * @param bound  视野范围描述类 {@link CameraBound}
     * @since 2.5.0
     */
    public void setCameraBound(CameraBound bound) {
        this.bridge.callHandler("displayer.setCameraBound", new Object[]{bound});
    }

    /**
     * 设置白板背景色（本地操作，不会同步）
     *
     * @param intColor 16 进制 aRGB 色值
     * @since 2.4.0
     */
    public void setBackgroundColor(int intColor) {
        if ((intColor & 0xFF000000) == 0xFF000000) {
            this.bridge.callHandler("displayer.background", new Object[]{Displayer.toHexString(intColor)});
            backgroundColor = intColor;
        } else {
            throw new AssertionError("alpha is not support to change");
        }
    }

    /**
     * 获取白板房间，本地背景色
     *
     * @return 16进制 aRGB 色值
     * @since 2.4.0
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    private final static String toHexString(int intColor) {
        return String.format("#%06X", (0xFFFFFF & intColor));
    }

    /**
     * 获取特定场景的全量截图
     *
     * @param scenePath 场景路径
     * @param promise   完成回调
     * @since 2.3.0
     */
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

    /**
     * 获取特定场景的预览图（用户切换到对应场景时，能看到的内容）
     *
     * @param scenePath 场景路径
     * @param promise   完成回调
     * @since 2.3.0
     */
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

    /**
     * 移动视角：移动，缩放白板
     *
     * @param camera 视角参数
     * @see CameraConfig 只需要传入，需要改动的值
     * @since 2.2.0
     */
    public void moveCamera(CameraConfig camera) {
        this.bridge.callHandler("displayer.moveCamera", new Object[]{camera});
    }

    /**
     * 调整用户视野
     *
     * @param rectangle 视野参数
     * @see RectangleConfig 需要传入完整的视野参数
     * @since 2.2.0
     */
    public void moveCameraToContainer(RectangleConfig rectangle) {
        this.bridge.callHandler("displayer.moveCameraToContain", new Object[]{rectangle});
    }
}
