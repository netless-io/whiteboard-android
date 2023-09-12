package com.herewhite.demo.test;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.herewhite.demo.R;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketTestActivity extends AppCompatActivity {
    private static final String TAG = WebSocketTestActivity.class.getSimpleName();
    OkHttpClient client = new OkHttpClient.Builder().build();
    private TextView display;
    private EditText messageInput;
    private Button connect;
    private Button close;
    private Button send;
    private WebSocket realWebSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_socket_test);

        display = findViewById(R.id.logDisplay);
        display.setMovementMethod(new ScrollingMovementMethod());

        messageInput = findViewById(R.id.editText);

        send = findViewById(R.id.sendMessage);
        connect = findViewById(R.id.connect);
        close = findViewById(R.id.close);

        connect.setOnClickListener(this::onConnect);
        close.setOnClickListener(this::onClose);
        send.setOnClickListener(this::onSendMessage);
    }

    private void onSendMessage(View view) {
        String message = messageInput.getText().toString();
        if (!message.equals("")) {
            if (realWebSocket != null) {
                realWebSocket.send(message);
            }
        }
    }

    private void onClose(View view) {
        if (realWebSocket != null) {
            realWebSocket.close(1000, "");
        }
    }

    private void onConnect(View view) {
        try {
            Request request = new Request.Builder().url("wss://echo.websocket.org").build();
            realWebSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    appendLog("onOpen");
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    appendLog("onMessage message " + text);
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    super.onClosing(webSocket, code, reason);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    appendLog("onClose reason " + reason);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    super.onFailure(webSocket, t, response);
                    {
                        appendLog("onError");
                    }
                }
            });
        } catch (Exception e) {
            appendLog("onConnect Exception :" + e);
        }
    }

    private void appendLog(String log) {
        display.append(log + "\n");
    }
}
