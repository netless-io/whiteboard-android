package com.herewhite.sdk.domain;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SceneTest {
    private Gson gson = new Gson();
    private static final Class[] PRIMITIVE_TYPES = { int.class, long.class, short.class,
            float.class, double.class, byte.class, boolean.class, char.class, Integer.class, Long.class,
            Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class };
    private static List<Class> PRIMITIVE_LIST = new ArrayList<>(Arrays.asList(PRIMITIVE_TYPES));

    private static boolean isPrimitiveOrStringOrNull(Object target) {
        if (target instanceof String) {
            return true;
        } else if (target == null) {
            return true;
        }

        return PRIMITIVE_LIST.contains(target.getClass());
    }

    private Object toJSON(Object object) {

        if (isPrimitiveOrStringOrNull(object)) {
            return object;
        } else if (object instanceof WhiteObject) {
            return ((WhiteObject) object).toJSON();
        } else {
            return gson.toJsonTree(object);
        }
    }

    @Test
    public void testJSON() {
        Scene[] s = new Scene[]{
                new Scene("page2", new PptPage("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/image/alin-rusu-1239275-unsplash_opt.jpg", 600d, 600d))
        };
        Object a = toJSON(s);
    }
}