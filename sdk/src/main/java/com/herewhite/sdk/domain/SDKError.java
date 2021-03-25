package com.herewhite.sdk.domain;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import androidx.annotation.Nullable;

/**
 * Created by buhe on 2018/8/18.
 */

public class SDKError extends Exception {

    private final String jsStack;
    static Gson gson = new Gson();

    public static @Nullable
    SDKError parseError(JSONObject object) {
        try {
            String msg = object.getString("message");
            String jsStack = object.getString("jsStack");
            return new SDKError(msg, jsStack);
        } catch (org.json.JSONException e) {
            return null;
        }
    }

    public static @Nullable
    SDKError promiseError(String str) {
        JsonObject jsonObject = gson.fromJson(str, JsonObject.class);
        return SDKError.promiseError(jsonObject);
    }

    public static @Nullable
    SDKError promiseError(JsonObject jsonObject) {
        if (jsonObject.has("__error")) {
            String msg = "Unknow exception";
            String jsStack = "Unknow stack";
            if (jsonObject.getAsJsonObject("__error").has("message")) {
                msg = jsonObject.getAsJsonObject("__error").get("message").getAsString();
            }
            if (jsonObject.getAsJsonObject("__error").has("jsStack")) {
                jsStack = jsonObject.getAsJsonObject("__error").get("jsStack").getAsString();
            }
            return new SDKError(msg, jsStack);
        } else {
            return null;
        }
    }

    public SDKError(String message) {
        super(message);
        jsStack = "Native Error";
    }

    public SDKError(String message, String jsStack) {
        super(message);
        this.jsStack = jsStack;
    }

    @Override
    public String toString() {
        return super.toString() + " jsStack: " + getJsStack();
    }

    public String getJsStack() {
        return jsStack;
    }
}
