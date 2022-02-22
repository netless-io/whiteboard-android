package com.herewhite.sdk.internal;

import static java.net.Proxy.Type.HTTP;

import android.content.Context;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.herewhite.sdk.JsBridgeInterface;
import com.herewhite.sdk.domain.WhiteObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import io.agora.fpa.proxy.FailedReason;
import io.agora.fpa.proxy.FpaHttpProxyChainConfig;
import io.agora.fpa.proxy.FpaProxyConnectionInfo;
import io.agora.fpa.proxy.FpaProxyService;
import io.agora.fpa.proxy.FpaProxyServiceConfig;
import io.agora.fpa.proxy.IFpaServiceListener;
import io.agora.fpa.proxy.LogLevel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WsJsInterfaceImpl {
    private static final String KEY_TYPE_BYTE_BUTTER = "arraybuffer";
    private static final String KEY_TYPE_STRING = "string";
    private static OkHttpClient client;

    private final JsBridgeInterface bridge;
    private final Context context;
    private WebSocket webSocket;

    public WsJsInterfaceImpl(JsBridgeInterface bridge, Context context) {
        this.bridge = bridge;
        this.context = context;
    }

    @JavascriptInterface
    public void setup(Object args) {
        Logger.info("ws interface setup " + args.toString());
        ensureSetupFpa();
        if (args instanceof JSONObject) {
            try {
                JSONObject jsonObject = (JSONObject) args;
                String url = jsonObject.getString("url");
                int key = jsonObject.getInt("key");
                webSocket = new WebSocketWrapper(url, key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Logger.error("ws send args error !!!", null);
        }
    }

    private void ensureSetupFpa() {
        if (client == null) {
            try {
                setupFpaService();
                setupChainConfig();
            } catch (Exception e) {
                Logger.error("ws create fpa service error ", e);
            }

            client = new OkHttpClient.Builder()
                    .readTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .proxy(createProxy())
                    .build();
        }
    }

    private void setupFpaService() throws Exception {
        File logFile = new File(context.getCacheDir().getAbsoluteFile() + "/whiteboard/fpa_log_sdk.log");
        FpaProxyService.getInstance().setListener(new IFpaServiceListener() {
            @Override
            public void onConnected(@Nullable FpaProxyConnectionInfo info) {
                Logger.info("[Fpa] onConnected. info=" + info);
            }

            @Override
            public void onAccelerationSuccess(@Nullable FpaProxyConnectionInfo info) {
                Logger.info("[Fpa] onAccelerationSuccess. info=" + info);
            }

            @Override
            public void onConnectionFailed(@Nullable FpaProxyConnectionInfo info, FailedReason reason) {
                Logger.info("[Fpa] onConnectionFailed. info=" + info + " reason=" + reason);
            }

            @Override
            public void onDisconnectedAndFallback(@Nullable FpaProxyConnectionInfo info, FailedReason reason) {
                Logger.info("[Fpa] onDisconnectedAndFallback. info=" + info + " reason=" + reason);
            }
        });

        FpaProxyServiceConfig config = new FpaProxyServiceConfig.Builder(logFile.getAbsolutePath())
                .setAppId("81ae40d666ed4fdc9b883962e9873a0b")
                .setLogFileSizeKb(1024)
                .setLogLevel(LogLevel.LOG_ERROR)
                .build();

        FpaProxyService.getInstance().start(config);
    }

    private void setupChainConfig() {
        FpaHttpProxyChainConfig chainConfig = new FpaHttpProxyChainConfig.Builder()
                .addChainInfo(285, "gateway.netless.link", 443, true)
                .fallbackWhenNoChainAvailable(true)
                .build();
        FpaProxyService.getInstance().setOrUpdateHttpProxyChainConfig(chainConfig);
    }

    private Proxy createProxy() {
        int port = FpaProxyService.getInstance().getHttpProxyPort();
        return port > 0 ? new Proxy(HTTP, new InetSocketAddress("127.0.0.1", port)) : null;
    }

    @JavascriptInterface
    public void send(Object args) {
        if (args instanceof JSONObject) {
            try {
                JSONObject jsonObject = (JSONObject) args;
                String type = jsonObject.getString("type");
                String data = jsonObject.getString("data");
                if (KEY_TYPE_BYTE_BUTTER.equals(type)) {
                    ByteString bs = ByteString.decodeBase64(data);
                    if (bs != null) {
                        webSocket.send(bs);
                    }
                } else if (KEY_TYPE_STRING.equals(type)) {
                    webSocket.send(data);
                } else {
                    Logger.error("ws send not support type " + args, null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Logger.error("ws send args error !!!", null);
        }
    }

    @JavascriptInterface
    public void close(Object args) {
        Logger.info("ws interface close " + args.toString());
        if (args instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args;
            try {
                int code = jsonObject.getInt("code");
                String reason = jsonObject.optString("reason", "");
                webSocket.close(code, reason);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Logger.error("ws close args error !!!", null);
        }
    }

    private class WebSocketWrapper implements WebSocket {
        private final int key;
        private final WebSocket realWebSocket;

        WebSocketWrapper(String url, int key) {
            Request request = new Request.Builder().url(url).build();
            realWebSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                    notifyJsOpen();
                }

                @Override
                public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                    notifyJsMessage(text, true);
                }

                @Override
                public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                    notifyJsMessage(bytes.base64(), false);
                }

                @Override
                public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                    // ignore
                }

                @Override
                public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                    notifyJsClosed(code, reason);
                }

                @Override
                public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                    notifyJsClosed(1006, "ws native onFailure called");
                    // notifyJsError(t.getMessage());
                }
            });
            this.key = key;
        }

        @NonNull
        @Override
        public Request request() {
            return realWebSocket.request();
        }

        @Override
        public long queueSize() {
            return realWebSocket.queueSize();
        }

        @Override
        public boolean send(@NonNull String text) {
            return realWebSocket.send(text);
        }

        @Override
        public boolean send(@NonNull ByteString byteString) {
            return realWebSocket.send(byteString);
        }

        @Override
        public boolean close(int code, String reason) {
            return realWebSocket.close(code, reason);
        }

        @Override
        public void cancel() {
            realWebSocket.cancel();
        }

        private void notifyJsOpen() {
            bridge.callHandler("ws.onOpen", new Object[]{new BaseMessage(key)});
        }

        private void notifyJsClosed(int code, String reason) {
            bridge.callHandler("ws.onClose", new Object[]{new WsClose(code, reason, key)});
        }

        private void notifyJsMessage(String data, boolean isText) {
            String type = isText ? KEY_TYPE_STRING : KEY_TYPE_BYTE_BUTTER;
            bridge.callHandler("ws.onMessage", new Object[]{new WsMessage(type, data, key)});
        }

        private void notifyJsError(String message) {
            bridge.callHandler("ws.onError", new Object[]{new WsError(message, key)});
        }

        class BaseMessage extends WhiteObject {
            private final int key;

            BaseMessage(int key) {
                this.key = key;
            }
        }

        class WsClose extends BaseMessage {
            private final int code;
            private final String reason;

            WsClose(int code, String reason, int key) {
                super(key);
                this.code = code;
                this.reason = reason;
            }
        }

        class WsError extends BaseMessage {
            private final String message;

            WsError(String message, int key) {
                super(key);
                this.message = message;
            }
        }

        class WsMessage extends BaseMessage {
            private final String type;
            private final String data;

            WsMessage(String type, String data, int key) {
                super(key);
                this.type = type;
                this.data = data;
            }
        }
    }
}
