package com.herewhite.sdk.implement;

import com.herewhite.sdk.Logger;
import com.herewhite.sdk.WhiteBroadView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import wendu.dsbridge.OnReturnValue;

public class BridgeWrapper {

    public BridgeWrapper(WhiteBroadView bridge) {
        this.bridge = bridge;
    }

    private final WhiteBroadView bridge;


    public <T> void callHandler(String method, Object[] args, OnReturnValue<T> handler) {
        this.bridge.callHandler(method, toMaps(args), handler);

    }

    public void callHandler(String method, Object[] args) {
        this.callHandler(method, args, (OnReturnValue) null);
    }

    public <T> void callHandler(String method, OnReturnValue<T> handler) {
        this.callHandler(method, (Object[]) null, handler);
    }

    private Object[] toMaps(Object[] objects) {
        if (objects != null) {
            Object[] maps = new Object[objects.length];
            for (int i = 0; i < objects.length; i++) {
                maps[i] = toMap(objects[i]);
            }
            return maps;
        } else {
            return new Object[0];
        }
    }

    private Object toMap(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Double
                || object instanceof Integer
                || object instanceof Long
                || object instanceof Float
                || object instanceof Short
                || object instanceof Boolean
                || object instanceof String) {
            return object;
        }
        Map result = new HashMap();
        Field[] allFields = object.getClass().getDeclaredFields();
        for (Field field : allFields) {
            try {
                field.setAccessible(true);
                Object value = field.get(object);
                if (value != null) {
                    result.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                Logger.error("convert object to map error, class: " + object.getClass() + " field:" + field.getName(), e);
            }
        }
        return result;
    }
}
