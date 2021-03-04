package com.herewhite.sdk;

import android.content.Context;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

class Utils {
    private final static Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    /**
     * @param object
     * @param classOfT
     * @param <T>
     * @return 返回深拷贝对象
     */
    public static <T> T deepCopy(T object, Class<T> classOfT) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(object), classOfT);
    }

    public static int getDensityDpi(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.densityDpi;
    }
}
