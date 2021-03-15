package com.herewhite.sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.FrequencyEventListener;
import com.herewhite.sdk.domain.Point;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RectangleConfig;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.WhiteObject;
import com.herewhite.sdk.domain.WhiteScenePathType;
import com.herewhite.sdk.internal.Logger;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.ColorInt;
import wendu.dsbridge.OnReturnValue;

/**
 * 白板房间基类
 */
public class Displayer {
    protected final static Gson gson = new Gson();
    @ColorInt
    private int backgroundColor = Color.WHITE;

    protected final JsBridgeInterface bridge;
    protected String uuid;
    protected int densityDpi;
    private Handler handler;

    protected ConcurrentHashMap<String, EventListener> eventListenerMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, FrequencyEventListener> frequencyEventListenerMap = new ConcurrentHashMap<>();

    public Displayer(String uuid, JsBridgeInterface bridge, int densityDpi) {
        this.uuid = uuid;
        this.bridge = bridge;
        this.densityDpi = densityDpi;
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    protected void post(Runnable runnable) {
        getHandler().post(runnable);
    }

    /**
     * 向 iframe 插件发送字符串信息
     *
     * @param string
     * @since 2.11.4
     */
    public void postIframeMessage(String string) {
        bridge.callHandler("displayer.postMessage", new Object[]{string});
    }

    /**
     * 向 iframe 插件发送 key-value 格式的信息。可以自己创建 WhiteObject 的子类进行发送
     *
     * @param object
     * @since 2.11.4
     */
    public void postIframeMessage(WhiteObject object) {
        bridge.callHandler("displayer.postMessage", new Object[]{object});
    }

    /**
     * 查询路径对应的内容，还是页面（场景），或者是页面（场景）目录，或者不存在任何内容。
     *
     * @param path    进行查询的路径
     * @param promise 回调结果，具体内容，可以查看 {@link WhiteScenePathType}
     */
    public void getScenePathType(String path, final Promise<WhiteScenePathType> promise) {
        bridge.callHandler("displayer.scenePathType", new Object[]{path}, new OnReturnValue<String>() {
            @Override
            public void onValue(String retValue) {
                WhiteScenePathType type = gson.fromJson(retValue, WhiteScenePathType.class);
                promise.then(type);
            }
        });
    }


    /**
     * 获取当前房间内所有的白板页面信息
     *
     * @param promise 返回 一个 map，key 为场景目录地址，value 为该目录下，所有 Scene 数组。
     */
    public void getEntireScenes(final Promise<Map<String, Scene[]>> promise) {
        bridge.callHandler("displayer.entireScenes", new OnReturnValue<JSONObject>() {
            @Override
            public void onValue(JSONObject retValue) {
                Type type = new TypeToken<Map<String, Scene[]>>() {
                }.getType();
                Map<String, Scene[]> map = gson.fromJson(String.valueOf(retValue), type);
                promise.then(map);
            }
        });
    }

    /**
     * 刷新当前白板的视觉矩形。
     *
     * @note 当 WhiteboardView 大小出现改变时，需要手动调用该方法。
     */
    public void refreshViewSize() {
        bridge.callHandler("displayer.refreshViewSize", new Object[]{});
    }

    /**
     * 以连续动画的形式，等比例缩放ppt，保证ppt所有内容都在容器内。
     *
     * @since 2.4.22
     */
    public void scalePptToFit() {
        bridge.callHandler("displayer.scalePptToFit", new Object[]{});
    }

    /**
     * 等比例缩放ppt，保证ppt所有内容都在容器内。
     *
     * @param mode 缩放时，动画行为
     * @since 2.4.28
     */
    public void scalePptToFit(AnimationMode mode) {
        String modeString = gson.fromJson(gson.toJson(mode), String.class);
        bridge.callHandler("displayer.scalePptToFit", new Object[]{modeString});
    }

    /**
     * 注册自定义事件监听，接受对应名称的自定义事件通知（包括自己发送的）。
     * 目前 Android 端，同一个自定义事件（名），只支持单个回调。只有 Web 端支持一个自定义事件，调用多个回调。
     *
     * @param eventName     需要监听自定义事件名称
     * @param eventListener 自定义事件回调；重复添加时，旧回调会被覆盖
     */
    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerMap.put(eventName, eventListener);
        bridge.callHandler("displayer.addMagixEventListener", new Object[]{eventName});
    }

    /**
     * 注册高频自定义事件监听，接受对应名称的自定义事件通知（包括自己发送的）。
     * 目前 Android 端，同一个自定义事件（名），只支持单个回调。只有 Web 端支持一个自定义事件，调用多个回调。
     *
     * @param eventName     需要监听自定义事件名称
     * @param eventListener 自定义事件回调；重复添加时，旧回调会被覆盖
     * @param fireInterval  调用频率, 单位：毫秒，最低 500ms，传入任何低于该值的数字，都会重置为 500ms
     */
    public void addHighFrequencyEventListener(String eventName, FrequencyEventListener eventListener, Integer fireInterval) {
        if (fireInterval < 500) {
            fireInterval = 500;
        }
        this.frequencyEventListenerMap.put(eventName, eventListener);
        bridge.callHandler("displayer.addHighFrequencyEventListener", new Object[]{eventName, fireInterval});
    }

    /**
     * 移除自定义事件监听
     * 目前 Android 端同一个自定义事件（名），只支持单个回调。移除时，只需要传入自定义事件名称即可。
     *
     * @param eventName 需要移除监听的自定义事件名称
     */
    public void removeMagixEventListener(String eventName) {
        this.eventListenerMap.remove(eventName);
        this.frequencyEventListenerMap.remove(eventName);
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
     * @param bound 视野范围描述类 {@link CameraBound}
     * @since 2.5.0
     */
    public void setCameraBound(CameraBound bound) {
        this.bridge.callHandler("displayer.setCameraBound", new Object[]{bound});
    }

    /**
     * 设置白板背景色（本地操作，不会同步）
     *
     * @param intColor 16 进制 aRGB,a 属性并不能达到使白板透明的效果
     * @since 2.4.14
     * @deprecated Android 端直接使用 {@link WhiteboardView#setBackgroundColor(int)} 即可
     */
    @Deprecated
    public void setBackgroundColor(@ColorInt int intColor) {
        Float[] rgba = hexSplit(intColor);
        this.bridge.callHandler("displayer.setBackgroundColor", rgba);
        backgroundColor = intColor;
    }

    private static Float[] hexSplit(@ColorInt int color) {
        Float r = Float.valueOf((color >> 16) & 0xff);
        Float g = Float.valueOf((color >> 8) & 0xff);
        Float b = Float.valueOf((color) & 0xff);
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
    public void getScenePreviewImage(String scenePath, final Promise<Bitmap> promise) {
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
    public void getSceneSnapshotImage(String scenePath, final Promise<Bitmap> promise) {
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
        final String pureBase64Encoded = base64String.substring(base64String.indexOf(",") + 1);
        final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = densityDpi;
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, opts);
    }

    /**
     * 禁止用户移动视角
     *
     * @param disable 禁止视角移动
     * @since 2.11.0
     */
    public void disableCameraTransform(Boolean disable) {
        bridge.callHandler("displayer.setDisableCameraTransform", new Object[]{disable});
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
