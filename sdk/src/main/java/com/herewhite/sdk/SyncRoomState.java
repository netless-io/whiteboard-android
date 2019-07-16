package com.herewhite.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herewhite.sdk.domain.RoomState;

import java.util.Map;
import java.util.Set;

public class SyncRoomState {

    private final static Gson gson = new Gson();
    private final static JsonParser parser = new JsonParser();

    private JsonObject stateJSON;

    public SyncRoomState(String stateJSON) {
        this.syncRoomStateAndCompareModifyStateJSON(stateJSON);
    }

    public JsonObject syncRoomStateAndCompareModifyStateJSON(String stateJSON) {
        JsonObject modifyStateJSON = parser.parse(stateJSON).getAsJsonObject();

        if (this.stateJSON == null) {
            this.stateJSON = modifyStateJSON;
            return null;

        } else {
            JsonObject jsonObject = new JsonObject();
            JsonObject checkedModifyStateJSON = null;

            for (String key: this.stateJSON.keySet()) {
                JsonElement originalValue = this.stateJSON.get(key);
                JsonElement newValue = modifyStateJSON.get(key);

                if (newValue != null) {
                    if (!compareJson(originalValue, newValue)) {
                        if (checkedModifyStateJSON == null) {
                            checkedModifyStateJSON = new JsonObject();
                        }
                        checkedModifyStateJSON.add(key, newValue);
                    }
                    jsonObject.add(key, newValue);

                } else {
                    jsonObject.add(key, originalValue);
                }
            }
            this.stateJSON = jsonObject;

            return checkedModifyStateJSON;
        }
    }

    public RoomState getRoomState() {
        return gson.fromJson(this.stateJSON, RoomState.class);
    }

    private static boolean compareJson(JsonElement json1, JsonElement json2) {
        boolean isEqual = true;

        // Check whether both jsonElement are not null
        if (json1 != null && json2 != null) {

            // Check whether both jsonElement are objects
            if (json1.isJsonObject() && json2.isJsonObject()) {
                Set<Map.Entry<String, JsonElement>> ens1 = ((JsonObject) json1).entrySet();
                Set<Map.Entry<String, JsonElement>> ens2 = ((JsonObject) json2).entrySet();
                JsonObject json2obj = (JsonObject) json2;
                if (ens1 != null && ens2 != null && (ens2.size() == ens1.size())) {
                    // Iterate JSON Elements with Key values
                    for (Map.Entry<String, JsonElement> en : ens1) {
                        isEqual = isEqual && compareJson(en.getValue(), json2obj.get(en.getKey()));
                    }
                } else {
                    return false;
                }
            }

            // Check whether both jsonElement are arrays
            else if (json1.isJsonArray() && json2.isJsonArray()) {
                JsonArray jarr1 = json1.getAsJsonArray();
                JsonArray jarr2 = json2.getAsJsonArray();
                if (jarr1.size() != jarr2.size()) {
                    return false;
                } else {
                    int i = 0;
                    // Iterate JSON Array to JSON Elements
                    for (JsonElement je : jarr1) {
                        isEqual = isEqual && compareJson(je, jarr2.get(i));
                        i++;
                    }
                }
            }

            // Check whether both jsonElement are null
            else if (json1.isJsonNull() && json2.isJsonNull()) {
                return true;
            }

            // Check whether both jsonElement are primitives
            else if (json1.isJsonPrimitive() && json2.isJsonPrimitive()) {
                if (json1.equals(json2)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (json1 == null && json2 == null) {
            return true;

        } else {
            return false;
        }
        return isEqual;
    }
}
