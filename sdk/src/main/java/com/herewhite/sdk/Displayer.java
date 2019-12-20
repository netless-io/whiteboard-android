package com.herewhite.sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.FrequencyEventListener;
import com.herewhite.sdk.domain.Point;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RectangleConfig;
import com.herewhite.sdk.domain.SDKError;

import java.util.concurrent.ConcurrentHashMap;

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

    protected ConcurrentHashMap<String, EventListener> eventListenerConcurrentHashMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, FrequencyEventListener> frequencyEventListenerConcurrentHashMap = new ConcurrentHashMap<>();

    public Displayer(String uuid, WhiteboardView bridge, Context context, WhiteSdk sdk) {
        this.uuid = uuid;
        this.bridge = bridge;
        this.context = context;
        this.sdk = sdk;
    }

    /**
     * 刷新当前白板的视觉矩形。
     * 当 WhiteboardView 大小出现改变时，需要手动调用该方法。
     */
    public void refreshViewSize() {
        bridge.callHandler("displayer.refreshViewSize", new Object[]{});
    }

    /**
     * 注册自定义事件监听，接受对应名称的自定义事件通知（包括自己发送的）。
     * 目前 Android 端，同一个自定义事件（名），只支持单个回调。只有 Web 端支持一个自定义事件，调用多个回调。
     * @param eventName     需要监听自定义事件名称
     * @param eventListener 自定义事件回调；重复添加时，旧回调会被覆盖
     */
    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerConcurrentHashMap.put(eventName, eventListener);
        bridge.callHandler("displayer.addMagixEventListener", new Object[]{eventName});
    }

    /**
     * 注册高频自定义事件监听，接受对应名称的自定义事件通知（包括自己发送的）。
     * 目前 Android 端，同一个自定义事件（名），只支持单个回调。只有 Web 端支持一个自定义事件，调用多个回调。
     * @param eventName     需要监听自定义事件名称
     * @param eventListener 自定义事件回调；重复添加时，旧回调会被覆盖
     * @param fireInterval 调用频率, 单位：毫秒，最低 500ms，传入任何低于该值的数字，都会重置为 500ms
     */
    public void addHighFrequencyEventListener(String eventName, FrequencyEventListener eventListener, Integer fireInterval) {
        if (fireInterval < 500) {
            fireInterval = 500;
        }
        this.frequencyEventListenerConcurrentHashMap.put(eventName, eventListener);
        bridge.callHandler("displayer.addHighFrequencyEventListener", new Object[]{eventName, fireInterval});
    }

    /**
     * 移除自定义事件监听
     * 目前 Android 端同一个自定义事件（名），只支持单个回调。移除时，只需要传入自定义事件名称即可。
     * @param eventName 需要移除监听的自定义事件名称
     */
    public void removeMagixEventListener(String eventName) {
        this.eventListenerConcurrentHashMap.remove(eventName);
        this.frequencyEventListenerConcurrentHashMap.remove(eventName);
        bridge.callHandler("displayer.removeMagixEventListener", new Object[]{eventName});
    }

    /**
     * 将以白板左上角为原点的 Android 坐标系坐标，转换为白板内部坐标系（坐标原点为白板初始化时中点位置，坐标轴方向相同）坐标
     *
     * @param x       the Android 端 x 坐标
     * @param y       the Android 端 y 坐标
     * @param promise 完成回调
     */
    public void convertToPointInWorld(double x, double y, final Promise<Point> promise) {
        bridge.callHandler("displayer.convertToPointInWorld", new Object[]{x, y}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(gson.fromJson(String.valueOf(o), Point.class));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from convertToPointInWorld", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in convertToPointInWorld promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
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
     * @param intColor 16 进制 aRGB,a 属性并不能达到使白板透明的效果
     * @deprecated Android 端直接使用 {@link WhiteboardView#setBackgroundColor(int)} 即可
     * @since 2.4.14
     */
    @Deprecated
    public void setBackgroundColor(@ColorInt int intColor) {
        Float[] rgba = hexSplit(intColor);
        this.bridge.callHandler("displayer.setBackgroundColor", rgba);
        backgroundColor = intColor;
    }

    private static Float[] hexSplit(@ColorInt int color) {
        Float r = Float.valueOf((color >> 16) & 0xff);
        Float g = Float.valueOf((color >>  8) & 0xff);
        Float b = Float.valueOf((color      ) & 0xff);
        Float a = Float.valueOf(((color >> 24) & 0xff) / 255.0f);
        return new Float[]{r, g, b, a};
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
