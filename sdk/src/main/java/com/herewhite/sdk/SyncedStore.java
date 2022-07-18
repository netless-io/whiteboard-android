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
    private final HashMap<String, Class<SyncedStoreObject>> types = new HashMap<>();
    private final HashMap<String, CopyOnWriteArraySet<OnStateChangedListener>> listenersByName = new HashMap<>();

    public SyncedStore(JsBridgeInterface bridge) {
        this.bridge = bridge;
    }

    public <T extends SyncedStoreObject> void connectStorage(String name, T data, Promise<T> promise) {
        types.put(name, (Class<SyncedStoreObject>) data.getClass());
        bridge.callHandler("store.connectStorage", new Object[]{name, data}, (OnReturnValue<String>) retValue -> {
            JsonObject state = Utils.fromJson(retValue, JsonObject.class);
            storages.put(name, state);
            promise.then(getStorageState(name));
        });
    }

    public <T extends SyncedStoreObject> T getStorageState(String name) {
        return (T) Utils.fromJson(storages.get(name), types.get(name));
    }

    public <T extends SyncedStoreObject> void getStorageState(String name, Promise<T> promise) {
        bridge.callHandler("store.getStorageState", new Object[]{name}, (OnReturnValue<String>) retValue -> {
            try {
                T state = (T) Utils.fromJson(retValue, types.get(name));
                promise.then(state);
            } catch (Exception e) {
                promise.catchEx(SDKError.promiseError(e.getMessage()));
            }
        });
    }

    public <T extends SyncedStoreObject> void setStorageState(String name, T data) {
        bridge.callHandler("store.setStorageState", new Object[]{name, data});
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

    public void fireStorageStateUpdate(String valueOf) {
        StorageStateUpdate stateUpdate = Utils.fromJson(valueOf, StorageStateUpdate.class);
        String name = stateUpdate.name;
        JsonObject data = stateUpdate.data;
        if (types.containsKey(name) && storages.containsKey(name)) {
            JsonObject newValueData = getNewValueObject(data);

            Class<SyncedStoreObject> storeObjectClass = types.get(name);
            JsonObject merged = mergeUpdate(storages.get(name).deepCopy(), newValueData);
            storages.put(stateUpdate.name, merged);
            SyncedStoreObject current = Utils.fromJson(merged, storeObjectClass);
            SyncedStoreObject modifyState = Utils.fromJson(newValueData, storeObjectClass);

            notifyStateChanged(stateUpdate.name, modifyState, current);
        }
    }

    private void notifyStateChanged(String name, SyncedStoreObject modifyState, SyncedStoreObject current) {
        CopyOnWriteArraySet<OnStateChangedListener> listeners = listenersByName.get(name);
        for (OnStateChangedListener listener : listeners) {
            listener.onStateChanged(modifyState, current);
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

    public <T extends SyncedStoreObject> void addOnStateChangedListener(String name, @NonNull OnStateChangedListener<T> listener) {
        CopyOnWriteArraySet<OnStateChangedListener> listeners = listenersByName.get(name);
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<>();
            listenersByName.put(name, listeners);
        }
        listeners.add(listener);
    }

    public <T extends SyncedStoreObject> void removeOnStateChangedListener(String name, OnStateChangedListener<T> listener) {
        CopyOnWriteArraySet<OnStateChangedListener> listeners = listenersByName.get(name);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public interface OnStateChangedListener<T extends SyncedStoreObject> {
        void onStateChanged(T value, T diff);
    }

    static class StorageStateUpdate {
        public String name;
        public JsonObject data;
    }
}
