package com.herewhite.demo.utils;

import androidx.annotation.VisibleForTesting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// for test
public class GsonUtils {
    private static JsonObject assignObject(JsonObject object, JsonObject update) {
        return (JsonObject) assignElement(object, update);
    }

    /**
     * 更新本地存储元素
     * 如果 update 为空，是为清除存储操作。
     * 如果 update 非空，使用 update 数据更新已有元，此过程为递归过程
     * @param element 本地存储元素副本
     * @param update  更新字段信息
     * @return 更新后元素
     */
    @VisibleForTesting
    static JsonElement assignElement(JsonElement element, JsonElement update) {
        if (!element.isJsonObject() || !update.isJsonObject()) {
            return update;
        }
        JsonObject obj1 = (JsonObject) element;
        JsonObject obj2 = (JsonObject) update;

        // empty call
        if (obj2.keySet().isEmpty()) {
            return obj2;
        }
        for (String key : obj2.keySet()) {
            if (obj1.has(key) && obj1.get(key).isJsonObject()) {
                // 存在则递归替换
                JsonElement merge = assignElement(obj1.get(key), obj2.get(key));
                obj1.add(key, merge);
            } else {
                obj1.add(key, obj2.get(key));
            }
        }
        return obj1;
    }
}
