package com.herewhite.sdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import androidx.annotation.ColorInt;
import androidx.annotation.VisibleForTesting;

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

import wendu.dsbridge.special.OnReturnValue;

/**
 * `Displayer` 类。该类为 {@link com.herewhite.sdk.Room Room} 类和 {@link com.herewhite.sdk.Player Player} 类的父类。
 * `Room` 和 `Player` 类可以继承该类下的方法。
 */
public class Displayer {
    protected final static Gson gson = new Gson();
    protected final JsBridgeInterface bridge;
    protected String uuid;
    protected int densityDpi;
    @VisibleForTesting
    ConcurrentHashMap<String, EventListener> eventListenerMap = new ConcurrentHashMap<>();
    @VisibleForTesting
    ConcurrentHashMap<String, FrequencyEventListener> frequencyEventListenerMap = new ConcurrentHashMap<>();
    @ColorInt
    private int backgroundColor = Color.WHITE;
    private Handler handler;
    private SyncedStore syncedStore;

    /// @cond test

    /**
     * Displayer 类的构造函数。
     *
     * @param uuid       白板房间的 UUID。
     * @param bridge     白板界面，详见 {@link WhiteboardView}。
     * @param densityDpi Android屏幕密度值。
     * 文档中隐藏
     */
    public Displayer(String uuid, JsBridgeInterface bridge, int densityDpi) {
        this.uuid = uuid;
        this.bridge = bridge;
        this.densityDpi = densityDpi;
    }
    /// @endcond

    private static Float[] hexSplit(@ColorInt int color) {
        Float r = Float.valueOf((color >> 16) & 0xff);
        Float g = Float.valueOf((color >> 8) & 0xff);
        Float b = Float.valueOf((color) & 0xff);
        Float a = Float.valueOf(((color >> 24) & 0xff) / 255.0f);
        return new Float[]{r, g, b, a};
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
     * 向 iframe 插件发送字符串信息。
     *
     * @since 2.11.4
     *
     * @param string 字符串格式的信息。
     */
    public void postIframeMessage(String string) {
        bridge.callHandler("displayer.postMessage", new Object[]{string});
    }

    /**
     * 向 iframe 插件发送 key-value 格式的信息。
     *
     * @since 2.11.4
     *
     * @param object key-value 格式的信息，必须为 {@link com.herewhite.sdk.domain.WhiteObject WhiteObject} 的子类。
     *
     */
    public void postIframeMessage(WhiteObject object) {
        bridge.callHandler("displayer.postMessage", new Object[]{object});
    }

    /**
     * 查询场景路径类型。
     *
     * 你可以在该方法中指定想要查询的场景路径，SDK 会返回该路径对应的场景类型。
     *
     * @param path    想要查询的场景路径。
     * @param promise `Promise<WhiteScenePathType>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getScenePathType` 方法的调用结果：
     *                - 如果方法调用成功，将返回场景路径类型。详见 {@link com.herewhite.sdk.domain.WhiteScenePathType WhiteScenePathType}。
     *                - 如果方法调用失败，将返回错误信息。
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
     * 查询场景信息。
     *
     * @param path    想要查询的场景路径。
     * @param promise `Promise<Scene>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。
     *                - 如果方法调用成功，将返回场景信息。详见 {@link com.herewhite.sdk.domain.Scene Scene}。
     *                - 如果方法调用失败，将返回错误信息。
     */
    public void getScene(String path, final Promise<Scene> promise) {
        bridge.callHandler("displayer.getScene", new Object[]{path}, new OnReturnValue<String>() {
            @Override
            public void onValue(String retValue) {
                Scene scene = gson.fromJson(retValue, Scene.class);
                promise.then(scene);
            }
        });
    }

    /**
     * 获取当前房间内所有场景的信息。
     *
     * @param promise `Promise<Map<String, Scene[]>>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getEntireScenes` 方法的调用结果：
     *                - 如果方法调用成功，将返回当前房间内所有场景的信息。
     *                - 如果方法调用失败，将返回错误信息。
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
     * 刷新白板的界面。
     * <p>
     * 当 `WhiteboardView` 出现改变时，需要手动调用该方法刷新白板的界面。
     */
    public void refreshViewSize() {
        bridge.callHandler("displayer.refreshViewSize", new Object[]{});
    }

    /**
     * 调整视角，以保证完整显示 H5 课件的内容。
     *
     * @since 2.12.13
     */
    public void scaleIframeToFit() {
        bridge.callHandler("displayer.scaleIframeToFit", new Object[]{});
    }

    /**
     * 以渐变模式调整视角，以保证完整显示 PPT 的内容。
     *
     * @since 2.4.22
     */
    public void scalePptToFit() {
        bridge.callHandler("displayer.scalePptToFit", new Object[]{});
    }

    /**
     * 根据指定的动画模式调整视角，以保证完整显示 PPT 的内容。
     *
     * @since 2.4.28
     *
     * @param mode 视角调整时的动画模式，详见 {@link com.herewhite.sdk.domain.AnimationMode AnimationMode}。
     */
    public void scalePptToFit(AnimationMode mode) {
        String modeString = gson.fromJson(gson.toJson(mode), String.class);
        bridge.callHandler("displayer.scalePptToFit", new Object[]{modeString});
    }

    /**
     * 注册自定义事件监听。
     *
     * 成功注册后，你可以接收到对应的自定义事件通知。
     *
     * @note 对于同名的自定义事件，SDK 仅支持触发一个回调。
     *
     * @param eventName     想要监听的自定义事件名称。
     * @param eventListener 自定义事件回调，详见 {@link com.herewhite.sdk.domain.EventListener EventListener}。如果添加多个同名的事件回调，则之前添加的回调会被覆盖。
     *
     */
    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerMap.put(eventName, eventListener);
        bridge.callHandler("displayer.addMagixEventListener", new Object[]{eventName});
    }

    /**
     * 注册高频自定义事件监听。
     *
     * 成功注册后，你可以接收到对应的自定义事件通知。
     *
     * @note 对于同名的自定义事件，SDK 仅支持触发一个回调。
     *
     * @param eventName     想要监听的自定义事件名称。
     * @param eventListener 自定义事件回调，详见 {@link com.herewhite.sdk.domain.FrequencyEventListener FrequencyEventListener}。如果添加多个同名的事件回调，则之前添加的回调会被覆盖。
     * @param fireInterval  SDK 触发回调的频率，单位为毫秒。该参数最小值为 500 ms，如果设置为低于该值会被重置为 500 ms。
     */
    public void addHighFrequencyEventListener(String eventName, FrequencyEventListener eventListener, Integer fireInterval) {
        if (fireInterval < 500) {
            fireInterval = 500;
        }
        this.frequencyEventListenerMap.put(eventName, eventListener);
        bridge.callHandler("displayer.addHighFrequencyEventListener", new Object[]{eventName, fireInterval});
    }

    /**
     * 移除自定义事件监听。
     *
     * @param eventName 想要移除监听的自定义事件名称。
     */
    public void removeMagixEventListener(String eventName) {
        this.eventListenerMap.remove(eventName);
        this.frequencyEventListenerMap.remove(eventName);
        bridge.callHandler("displayer.removeMagixEventListener", new Object[]{eventName});
    }

    /**
     * 转换白板上点的坐标。
     * <p>
     * 该方法可以将 Android 内部坐标系（以屏幕左上角为原点）中的坐标转换为世界坐标系（以白板初始化时的中点为原点）坐标。
     *
     * @param x       点在 Android 坐标系中的 X 轴坐标，单位为DP。
     * @param y       点在 Android 坐标系中的 Y 轴坐标，单位为DP。
     * @param promise `Promise<Point>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `convertToPointInWorld` 的调用结果：
     *                - 如果方法调用成功，将返回点在世界坐标系上的坐标。详见 {@link com.herewhite.sdk.domain.Point Point}。
     *                - 如果方法调用失败，将返回错误信息。
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
     * 设置视角边界。
     *
     * @since 2.5.0
     *
     * @param bound 视角边界，详见 {@link com.herewhite.sdk.domain.CameraBound CameraBound}。
     */
    public void setCameraBound(CameraBound bound) {
        this.bridge.callHandler("displayer.setCameraBound", new Object[]{bound});
    }

    /**
     * 获取本地白板的背景色。
     *
     * @since 2.4.0
     *
     * @deprecated 该方法已废弃。
     *
     * @return 本地白板的背景色，格式为 16 进制 ARGB 定义下的 Hex 值。
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * 设置白板的背景色。
     *
     * @since 2.4.14
     *
     * @deprecated 该方法已废弃，请改用 Android 系统的 [setBackgroundColor](https://developer.android.com/reference/android/view/View#setBackgroundColor(int)) 方法。
     *
     * @note 该方法仅对本地白板有效，不会影响房间内其他用户白板的背景色。
     *
     * @param intColor 白板的背景色，格式为 16 进制 RGBA 定义下的 Hex 值。注意 A 属性不能达到使白板透明的效果。
     */
    @Deprecated
    public void setBackgroundColor(@ColorInt int intColor) {
        Float[] rgba = hexSplit(intColor);
        this.bridge.callHandler("displayer.setBackgroundColor", rgba);
        backgroundColor = intColor;
    }

    /**
     * 获取指定场景的预览图。
     *
     * @since 2.3.0
     *
     * @param scenePath 场景路径。
     * @param promise   `Promise<Bitmap>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getScenePreviewImage` 方法的调用结果：
     *                  - 如果方法调用成功，将返回指定场景的预览图。
     *                  - 如果方法调用失败，将返回错误码。
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
     * 获取指定场景的截图。
     *
     * @since 2.3.0
     *
     * @param scenePath 场景路径。
     * @param promise   `Promise<Bitmap>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `getSceneSnapshotImage` 方法的调用结果：
     *                  - 如果方法调用成功，将返回指定场景的截图。
     *                  - 如果方法调用失败，将返回错误信息。
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
     * 禁止/允许用户调整视角。
     *
     * @since 2.11.0
     *
     * 该方法用于禁止或允许用户通过触屏手势移动或缩放视角。
     *
     * @param disable 是否禁止用户调整视角：
     *                - `true`: 禁止用户调整视角。
     *                - `false`: (默认) 允许用户调整视角。
     */
    public void disableCameraTransform(Boolean disable) {
        bridge.callHandler("displayer.setDisableCameraTransform", new Object[]{disable});
    }

    /**
     * 调整视角。
     *
     * @since 2.2.0
     *
     * 调用该方法后，SDK 会根据传入的参数调整视角。
     *
     * @param camera 视角的参数配置，详见 {@link com.herewhite.sdk.domain.CameraConfig CameraConfig}。
     */
    public void moveCamera(CameraConfig camera) {
        this.bridge.callHandler("displayer.moveCamera", new Object[]{camera});
    }

    /**
     * 调整视角，以保证完整显示视觉矩形。
     *
     * @since 2.2.0
     *
     * @param rectangle 视觉矩形的参数设置，详见 {@link com.herewhite.sdk.domain.RectangleConfig RectangleConfig}。
     */
    public void moveCameraToContainer(RectangleConfig rectangle) {
        this.bridge.callHandler("displayer.moveCameraToContain", new Object[]{rectangle});
    }

    /**
     * 获取当前多窗口状态
     * @param promise
     */
    public void getWindowManagerAttributes(final Promise<String> promise) {
        bridge.callHandler("displayer.getWindowManagerAttributes", new OnReturnValue<JSONObject>() {
            @Override
            public void onValue(JSONObject retValue) {
                promise.then(retValue.toString());
            }
        });
    }

    public SyncedStore getSyncedStore() {
        if (syncedStore == null) {
            syncedStore = new SyncedStore(bridge);
        }
        return syncedStore;
    }
}
