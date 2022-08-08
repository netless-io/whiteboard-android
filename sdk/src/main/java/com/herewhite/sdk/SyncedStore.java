package com.herewhite.sdk;

import androidx.annotation.NonNull;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import wendu.dsbridge.OnReturnValue;

public class SyncedStore {
    private final JsBridgeInterface bridge;
    private final HashMap<String, JsonObject> storages = new HashMap<>();
    private final HashMap<String, CopyOnWriteArraySet<OnStateChangedListener>> listenersByName = new HashMap<>();

    public SyncedStore(JsBridgeInterface bridge) {
        this.bridge = bridge;
    }

    public void connectStorage(String name, String defaultJson, Promise<String> promise) {
        bridge.callHandler("store.connectStorage", new Object[]{name, Utils.asJSONObject(defaultJson)}, (OnReturnValue<String>) retValue -> {
            SDKError sdkError = SDKError.promiseError(retValue);
            if (sdkError == null) {
                JsonObject state = Utils.fromJson(retValue, JsonObject.class);
                storages.put(name, state);
                promise.then(retValue);
            } else {
                promise.catchEx(sdkError);
            }
        });
    }

    public String getStorageState(String name) {
        return Utils.toJson(storages.get(name));
    }

    public void getStorageState(String name, Promise<String> promise) {
        bridge.callHandler("store.getStorageState", new Object[]{name}, (OnReturnValue<String>) retValue -> {
            try {
                promise.then(retValue);
            } catch (Exception e) {
                promise.catchEx(SDKError.promiseError(e.getMessage()));
            }
        });
    }

    public void setStorageState(String name, String json) {
        bridge.callHandler("store.setStorageState", new Object[]{name, Utils.asJSONObject(json)});
    }

    /**
     * Disconnect from synced storage and release listeners
     *
     * @param name : storage name
     */
    public void disconnectStorage(String name) {
        bridge.callHandler("store.disconnectStorage", new Object[]{name});
        listenersByName.remove(name);
    }

    /**
     * Delete storage index with all of its data and destroy the Storage instance.
     * @param name : storage name
     */
    public void deleteStorage(String name) {
        bridge.callHandler("store.deleteStorage", new Object[]{name});
    }

    /**
     * reset storage state to default state
     *
     * @param name
     */
    public void resetState(String name) {
        bridge.callHandler("store.resetState", new Object[]{name});
    }

    void fireStorageStateUpdate(String valueOf) {
        StorageStateUpdate stateUpdate = Utils.fromJson(valueOf, StorageStateUpdate.class);
        String name = stateUpdate.name;
        JsonObject data = stateUpdate.data;
        if (storages.containsKey(name)) {
            JsonObject merged = mergeUpdate(storages.get(name).deepCopy(), getNewValueObject(data));
            storages.put(stateUpdate.name, merged);

            String value = Utils.toJson(merged);
            String diff = Utils.toJsonWithNull(data);

            notifyStateChanged(stateUpdate.name, value, diff);
        }
    }

    private void notifyStateChanged(String name, String value, String diff) {
        CopyOnWriteArraySet<OnStateChangedListener> listeners = listenersByName.get(name);
        if (listeners == null) {
            return;
        }
        for (OnStateChangedListener listener : listeners) {
            listener.onStateChanged(value, diff);
        }
    }

    /**
     * SyncedStore 约定只保证一层数据变更。
     * update 格式类型 {"key":{"oldValue":{},"newValue":{}}}
     * @param update
     * @return
     */
    private static JsonObject getNewValueObject(JsonObject update) {
        JsonObject result = new JsonObject();
        for (String key : update.keySet()) {
            JsonObject updateItem = update.get(key).getAsJsonObject();
            if (updateItem.has("newValue")) {
                result.add(key, updateItem.get("newValue"));
            } else {
                result.add(key, JsonNull.INSTANCE);
            }
        }
        return result;
    }

    private static JsonObject mergeUpdate(JsonObject object, JsonObject update) {
        for (String key : update.keySet()) {
            object.add(key, update.get(key));
        }
        return object;
    }

    public void addOnStateChangedListener(String name, @NonNull OnStateChangedListener listener) {
        CopyOnWriteArraySet<OnStateChangedListener> listeners = listenersByName.get(name);
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<>();
            listenersByName.put(name, listeners);
        }
        listeners.add(listener);
    }

    public void removeOnStateChangedListener(String name, OnStateChangedListener listener) {
        CopyOnWriteArraySet<OnStateChangedListener> listeners = listenersByName.get(name);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public interface OnStateChangedListener {
        /**
         * @param value 当前状态值对象
         * @param diff 变更字段 json 表示
         */
        void onStateChanged(String value, String diff);
    }

    static class StorageStateUpdate {
        public String name;
        public JsonObject data;
    }
}
