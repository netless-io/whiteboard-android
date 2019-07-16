package com.herewhite.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.herewhite.sdk.domain.RoomPhase;

import java.util.Map;
import java.util.Set;

public class SyncDisplayerState<T> {

    private final static Gson gson = new Gson();
    private final static JsonParser parser = new JsonParser();

    private final Class<T> clazz;
    private final boolean disableCallbackWhilePutting;

    private JsonObject stateJSON;
    private RoomPhase phase;
    private Listener<T> listener;

    public SyncDisplayerState(Class<T> clazz, String stateJSON, RoomPhase phase, boolean disableCallbackWhilePutting) {
        this.clazz = clazz;
        this.phase = phase;
        this.disableCallbackWhilePutting = disableCallbackWhilePutting;
        this.syncDisplayerState(stateJSON);
    }

    public interface Listener<T> {
        void onDisplayerStateChanged(T modifyState);
    }

    public T getDisplayerState() {
        return gson.fromJson(this.stateJSON, this.clazz);
    }

    public RoomPhase getPhase() {
        return phase;
    }

    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }

    public void syncDisplayerState(String stateJSON) {
        JsonObject modifyStateJSON = this.putDisplayerStateAndCompareModifyStateJSON(parser.parse(stateJSON).getAsJsonObject());
        if (modifyStateJSON != null && this.listener != null) {
            T modifyState = gson.fromJson(modifyStateJSON, this.clazz);
            this.listener.onDisplayerStateChanged(modifyState);
        }
    }

    public void syncRoomPhase(RoomPhase phase) {
        this.phase = phase;
    }

    public void putDisplayerStateProperty(String key, Object value) {
        JsonElement originalValue = this.stateJSON.get(key);

        if (originalValue != null) {
            JsonElement newValue = assignObject(this.stateJSON.get(key), gson.toJsonTree(value));

            if (!compareJson(originalValue, newValue)) {
                JsonObject newStateJSON = new JsonObject();
                for (String otherKey: this.stateJSON.keySet()) {
                    if (otherKey.equals(key)) {
                        newStateJSON.add(otherKey, newValue);
                    } else {
                        newStateJSON.add(otherKey, this.stateJSON.get(otherKey));
                    }
                }
                this.stateJSON = newStateJSON;

                if (!this.disableCallbackWhilePutting) {
                    JsonObject modifyStateJSON = new JsonObject();
                    modifyStateJSON.add(key, newValue);
                    T modifyState = gson.fromJson(modifyStateJSON, this.clazz);
                    this.listener.onDisplayerStateChanged(modifyState);
                }
            }
        }
    }

    private JsonObject putDisplayerStateAndCompareModifyStateJSON(JsonObject modifyStateJSON) {

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

    private static JsonElement assignObject(JsonElement value1, JsonElement value2) {
        if (!value1.isJsonObject() || !value2.isJsonObject()) {
            return value2;
        } else {
            JsonObject object1 = (JsonObject) value1;
            JsonObject object2 = (JsonObject) value2;
            JsonObject newObject = new JsonObject();

            for (String key: object1.keySet()) {
                if (object2.has(key)) {
                    newObject.add(key, object2.get(key));
                } else {
                    newObject.add(key, object1.get(key));
                }
            }
            for (String key: object2.keySet()) {
                if (!newObject.has(key)) {
                    newObject.add(key, object2.get(key));
                }
            }
            return newObject;
        }
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
