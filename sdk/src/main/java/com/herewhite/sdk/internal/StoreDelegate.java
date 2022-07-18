package com.herewhite.sdk.internal;


/**
 * 内部接口，解耦 JsBridgeInterface 与 SyncedStore
 */
public interface StoreDelegate {
    void fireSyncedStoreUpdate(String value);
}
