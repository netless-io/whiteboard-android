package com.herewhite.sdk.internal;

import static java.net.Proxy.Type.HTTP;

import android.webkit.JavascriptInterface;

import com.herewhite.sdk.JsBridgeInterface;
import com.herewhite.sdk.domain.WhiteObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.agora.fpaService.FpaConfig;
import io.agora.fpaService.FpaService;
import io.agora.fpaService.LogUtil;
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

    private JsBridgeInterface bridge;
    private WebSocket webSocket;

    public WsJsInterfaceImpl(JsBridgeInterface bridge) {
        this.bridge = bridge;
    }

    private Map<String, Integer> createChainIdTable() {
        Map<String, Integer> chainIdTable = new HashMap<>();
        chainIdTable.put("gateway.netless.link:443", 285);
        return chainIdTable;
    }

    private Proxy createProxy() {
        Proxy result = null;
        try {
            FpaConfig config = new FpaConfig();
            config.setAppId("81ae40d666ed4fdc9b883962e9873a0b");
            config.setToken("81ae40d666ed4fdc9b883962e9873a0b");
            config.setChainIdTable(createChainIdTable());
            config.setLogLevel(1);
            config.setLogFilePath("/sdcard/test.log");

            FpaService fpaService = FpaService.createFpaService(config);
            result = new Proxy(HTTP, new InetSocketAddress("127.0.0.1", fpaService.getHttpProxyPort()));
        } catch (Exception e) {
            Logger.info("ws create fpa service error " + e.toString());
        }
        return result;
    }

    private void setupFpa() {
        if (client == null) {
            LogUtil.DEBUG = true;
            client = new OkHttpClient.Builder()
                    .readTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .proxy(createProxy())
                    .build();
        }
    }

    @JavascriptInterface
    public void setup(Object args) {
        setupFpa();
        Logger.info("ws interface setup " + args.toString());
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

    @JavascriptInterface
    public void send(Object args) {
        Logger.info("ws interface send " + args.toString());
        if (args instanceof JSONObject) {
            try {
                JSONObject jsonObject = (JSONObject) args;
                String type = jsonObject.getString("type");
                String data = jsonObject.getString("data");
                if (KEY_TYPE_BYTE_BUTTER.equals(type)) {
                    ByteString bs = ByteString.decodeBase64(data);
                    webSocket.send(bs);
                } else if (KEY_TYPE_STRING.equals(type)) {
                    webSocket.send(data);
                } else {
                    Logger.error("ws send not support type " + args.toString(), null);
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
        private WebSocket realWebSocket;

        WebSocketWrapper(String url, int key) {
            Request request = new Request.Builder().url(url).build();
            realWebSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    notifyJsOpen();
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    notifyJsMessage(text, true);
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    notifyJsMessage(bytes.base64(), false);
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    // ignore
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    notifyJsClosed(code, reason);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    notifyJsClosed(1006, "ws native onFailure called");
                    // notifyJsError(t.getMessage());
                }
            });
            this.key = key;
        }

        @Override
        public Request request() {
            return realWebSocket.request();
        }

        @Override
        public long queueSize() {
            return realWebSocket.queueSize();
        }

        @Override
        public boolean send(String text) {
            return realWebSocket.send(text);
        }

        @Override
        public boolean send(ByteString byteString) {
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
