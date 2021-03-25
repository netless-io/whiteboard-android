package com.herewhite.sdk;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Set;

public class CommonTestTools {
    public static boolean compareJson(String json1, String json2) {
        return compareJson(JsonParser.parseString(json1), JsonParser.parseString(json2));
    }

    public static boolean compareJson(JsonElement json1, JsonElement json2) {
        if (json1 == null && json2 == null) {
            return true;
        }

        if (json1.isJsonNull() && json2.isJsonNull()) {
            return true;
        } else if (json1.isJsonPrimitive() && json2.isJsonPrimitive()) {
            return json1.equals(json2);
        }

        boolean isEqual = true;
        if (json1.isJsonObject() && json2.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> ens1 = ((JsonObject) json1).entrySet();
            Set<Map.Entry<String, JsonElement>> ens2 = ((JsonObject) json2).entrySet();
            JsonObject json2obj = (JsonObject) json2;
            if (ens1 != null && ens2 != null && (ens2.size() == ens1.size())) {
                for (Map.Entry<String, JsonElement> en : ens1) {
                    isEqual = isEqual && compareJson(en.getValue(), json2obj.get(en.getKey()));
                }
            } else {
                return false;
            }
        } else if (json1.isJsonArray() && json2.isJsonArray()) {
            JsonArray jarr1 = json1.getAsJsonArray();
            JsonArray jarr2 = json2.getAsJsonArray();
            if (jarr1.size() != jarr2.size()) {
                return false;
            } else {
                int i = 0;
                for (JsonElement je : jarr1) {
                    isEqual = isEqual && compareJson(je, jarr2.get(i));
                    i++;
                }
            }
        } else {
            return false;
        }
        return isEqual;
    }
}
