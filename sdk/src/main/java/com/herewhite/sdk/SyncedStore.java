package com.herewhite.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.gson.JsonElement;
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
        JsonElement data = stateUpdate.data;
        if (types.containsKey(name) && storages.containsKey(name)) {
            Class<SyncedStoreObject> storeObjectClass = types.get(name);
            JsonObject merge = assignObject(storages.get(name).deepCopy(), stateUpdate.data);
            storages.put(stateUpdate.name, merge);

            SyncedStoreObject current = Utils.fromJson(merge, storeObjectClass);
            SyncedStoreObject modifyState = Utils.fromJson(data, storeObjectClass);

            notifyStateChanged(stateUpdate.name, modifyState, current);
        }
    }

    private void notifyStateChanged(String name, SyncedStoreObject modifyState, SyncedStoreObject current) {
        CopyOnWriteArraySet<OnStateChangedListener> listeners = listenersByName.get(name);
        for (OnStateChangedListener listener : listeners) {
            listener.onStateChanged(modifyState, current);
        }
    }

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
        void onStateChanged(T diff, T newValue);
    }

    static class StorageStateUpdate {
        public String name;
        public JsonObject data;
    }
}
