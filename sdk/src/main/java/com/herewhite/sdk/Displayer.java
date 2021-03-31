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
import androidx.annotation.VisibleForTesting;
import wendu.dsbridge.OnReturnValue;

/**
 * Displayer 类。该类为白板房间的基类。
 */
public class Displayer {
    protected final static Gson gson = new Gson();
    @ColorInt
    private int backgroundColor = Color.WHITE;

    protected final JsBridgeInterface bridge;
    protected String uuid;
    protected int densityDpi;
    private Handler handler;

    @VisibleForTesting
    ConcurrentHashMap<String, EventListener> eventListenerMap = new ConcurrentHashMap<>();
    @VisibleForTesting
    ConcurrentHashMap<String, FrequencyEventListener> frequencyEventListenerMap = new ConcurrentHashMap<>();

    /**
     * Displayer 类的构造函数。
     *
     * @param uuid       用户 ID。
     * @param bridge     白板界面，详见 {@link WhiteboardView}。
     * @param densityDpi Android屏幕密度值。
     */
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
     * 向 iframe 插件发送字符串信息。
     *
     * @param string 字符串格式的信息。
     * @since 2.11.4
     */
    public void postIframeMessage(String string) {
        bridge.callHandler("displayer.postMessage", new Object[]{string});
    }

    /**
     * 向 iframe 插件发送 key-value 格式的信息。
     * <p>
     * 你可以通过创建 WhiteObject 的子类来创建一个 ley-value 格式的信息。
     *
     * @param object `Whiteobject` 的子类，详见 {@link WhiteObject}。
     * @since 2.11.4
     */
    public void postIframeMessage(WhiteObject object) {
        bridge.callHandler("displayer.postMessage", new Object[]{object});
    }

    /**
     * 查询场景路径类型。
     * <p>
     * 你可以在该方法中指定想要查询的场景路径，SDK 会返回该路径对应的场景类型，是场景，还是场景目录，或者不存在任何内容。
     *
     * @param path    想要查询的场景类型。
     * @param promise `Promise<WhiteScenePathType>` 接口实例，详见 {@link WhiteScenePathType}。你可以通过该接口获取查询场景路径类型的结果：
     *                - 如果查询成功，将返回场景路径类型。
     *                - 如果查询失败，将返回错误信息。
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
     * 获取当前房间内所有的白板页面信息。
     *
     * @param promise `Promise<Map<String, Scene[]>>` 接口实例，详见 {@link Promise<Map<String, Scene[]>>}。你可以通过该接口查询获取当前房间
     *                内所有白板页面信息的结果：
     *                - 如果查询成功，将返回当前房间内所有的白板页面信息。该返回值为 map 格式，其中 key 为场景路径，value 为该路径下所有的场景数据。
     *                - 如果查询失败，将返回错误信息。
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
     * <p>
     * 当 WhiteboardView 大小出现改变时，需要手动调用该方法。
     */
    public void refreshViewSize() {
        bridge.callHandler("displayer.refreshViewSize", new Object[]{});
    }

    /**
     * 以连续动画的形式等比例缩放 PPT。
     * <p>
     * 该方法用于确保 PPT 页面的所有内容都在视野范围内。
     *
     * @since 2.4.22
     */
    public void scalePptToFit() {
        bridge.callHandler("displayer.scalePptToFit", new Object[]{});
    }

    /**
     * 等比例缩放 PPT。
     * <p>
     * 该方法用于确保 PPT 页面的内容都在视野内。
     *
     * @param mode PPT 缩放时的动画行为，详见 {@link AnimationMode}。
     * @since 2.4.28
     */
    public void scalePptToFit(AnimationMode mode) {
        String modeString = gson.fromJson(gson.toJson(mode), String.class);
        bridge.callHandler("displayer.scalePptToFit", new Object[]{modeString});
    }

    /**
     * 注册自定义事件监听。成功注册后，你可以接收到对应的自定义事件通知。
     *
     * @param eventName     想要监听的自定义事件名称。
     * @param eventListener 自定义事件回调，详见 {@link EventListener}。如果添加多个事件回调，则之前添加的回调会被覆盖。
     * @note 对于同名的自定义事件，SDK 仅支持触发一个回调。
     */
    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerMap.put(eventName, eventListener);
        bridge.callHandler("displayer.addMagixEventListener", new Object[]{eventName});
    }

    /**
     * 注册高频自定义事件监听。成功注册后，你可以接收到对应的自定义事件通知。
     *
     * @param eventName     想要监听的自定义事件名称。
     * @param eventListener 自定义事件回调，详见 {@link FrequencyEventListener}。如果添加多个事件回调，则之前添加的回调会被覆盖。
     * @param fireInterval  SDK 触发回调的频率，单位为毫秒。该参数最小值为 500ms；低于该值会被传入重置为 500ms。
     * @note 对于同名的自定义事件，SDK 仅支持触发一个回调。
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
     * 该方法可以将 Android 内部坐标系（以屏幕左上角为原点，横轴为 X 轴，正方向向右，纵轴为 Y 轴，正方向向下）中的坐标转换为白板内部坐标系（以白板初始化时的中点为原点，横轴为 X 轴，正方向向右，纵轴为 Y 轴，正方向向下）坐标。
     *
     * @param x       点在 Android 坐标系中的 X 轴坐标。
     * @param y       点在 Android 坐标系中的 Y 轴坐标。
     * @param promise 'Promise<Point>' 接口实例，详见 {@link Promise<> Promise<T>}。你可以通过该接口获取 `convertToPointInWorld` 的调用结果：
     *                - 如果方法调用成功，将返回点在白板内部坐标系上的坐标。
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
     * 设置视野范围。
     *
     * @param bound 视野范围，详见 {@link CameraBound}。
     * @since 2.5.0
     */
    public void setCameraBound(CameraBound bound) {
        this.bridge.callHandler("displayer.setCameraBound", new Object[]{bound});
    }

    /**
     * 设置白板背景色。
     *
     * @param intColor 白板的背景色，格式为 16 进制，ARGB 定义下的 Hex 值。注意 A 属性不能达到使白板透明的效果。
     * @note 该方法仅对本地显示有效，不会同步到频道内其他用户。
     * @since 2.4.14
     * @deprecated 该方法已废弃，请改用 {@link WhiteboardView#setBackgroundColor(int)}。
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
     * 获取本地白板的背景色。
     *
     * @return 本地白板的背景色。，格式为 16 进制 ARGB 定义下的 Hex 值。
     * @since 2.4.0
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * 获取特定场景的预览图。
     * <p>
     * 该方法可用于实现用户切换到对应场景时，能立刻看到该场景内容的功能。
     *
     * @param scenePath 指定的场景路径。
     * @param promise   `Promise<Bitmap>` 接口实例，详见 {@link Promise}。你可以通过该接口了解获取场景预览图的结果：
     *                  - 如果获取成功，将返回获取的预览图。
     *                  - 如果获取失败，将反馈错误码。
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
     * 获取特定场景的截图。
     * <p>
     * 该方法可用于实现用户切换到对应场景时，能立刻看到该场景内容的功能。
     *
     * @param scenePath 指定的场景路径。
     * @param promise   `Promise<Bitmap>` 接口实例，详见 {@link Promise}。你可以通过该接口了解获取场景截图的结果：
     *                  - 如果获取成功，将返回获取的截图。
     *                  - 如果获取失败，将返回错误信息。
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
     * 禁止/允许用户移动视角。
     * <p>
     * 移动视角是指用户可以通过触屏手势放大或缩小白板视野。
     *
     * @param disable 是否禁止用户移动视角：
     *                - true: 禁止用户移动视角。
     *                - false: (默认) 允许用户移动视角。
     * @since 2.11.0
     */
    public void disableCameraTransform(Boolean disable) {
        bridge.callHandler("displayer.setDisableCameraTransform", new Object[]{disable});
    }

    /**
     * 移动视角。
     * <p>
     * 该方法可用于实现用户通过触屏手势对白板视野进行缩放操作的功能。
     *
     * @param camera 移动视角的具体参数配置，详见 {@link CameraConfig}。
     * @since 2.2.0
     */
    public void moveCamera(CameraConfig camera) {
        this.bridge.callHandler("displayer.moveCamera", new Object[]{camera});
    }

    /**
     * 调整用户视野。
     *
     * @param rectangle 能表示用户视野的视觉矩形设置，详见 {@link RectangleConfig}。
     * @since 2.2.0
     */
    public void moveCameraToContainer(RectangleConfig rectangle) {
        this.bridge.callHandler("displayer.moveCameraToContain", new Object[]{rectangle});
    }
}
