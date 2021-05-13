package com.herewhite.sdk;

import android.content.Context;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.WhiteObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    // region bridge data convert

    /**
     * 转换成DWebView中需要类型
     *
     * @param objects
     * @return
     */
    public static Object[] toBridgeMaps(Object[] objects) {
        if (objects != null) {
            Object[] maps = new Object[objects.length];
            for (int i = 0; i < objects.length; i++) {
                maps[i] = toJSON(objects[i]);
            }
            return maps;
        } else {
            return new Object[0];
        }
    }

    private static final Class[] PRIMITIVE_TYPES = {int.class, long.class, short.class,
            float.class, double.class, byte.class, boolean.class, char.class, Integer.class, Long.class,
            Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class};
    private static List<Class> PRIMITIVE_LIST = Arrays.asList(PRIMITIVE_TYPES);

    private static boolean isPrimitiveOrStringOrNull(Object target) {
        if (target instanceof String) {
            return true;
        } else if (target == null) {
            return true;
        }

        return PRIMITIVE_LIST.contains(target.getClass());
    }

    private static Object toJSON(Object object) {
        // TODO: 当 object 内部其实也是标准的 JSON 结构时，最好不把 JSON stringify 成 string，而是保持原有结构传给 web 端
        // 这个改动，需要确认原有 API，之前大部分 web 的 API，都是接受 string 传入的
        if (object instanceof String[]) {
            return object;
        } else if (isPrimitiveOrStringOrNull(object)) {
            return object;
        } else if (object instanceof WhiteObject) {
            return ((WhiteObject) object).toJSON();
        } else if (object instanceof WhiteObject[]) {
            List<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < ((WhiteObject[]) object).length; i++) {
                list.add(((WhiteObject[]) object)[i].toJSON());
            }
            return list;
        } else {
            return gson.toJson(object);
        }
    }
    // region end
}
