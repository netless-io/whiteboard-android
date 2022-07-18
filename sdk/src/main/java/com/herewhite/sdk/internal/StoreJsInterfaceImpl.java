package com.herewhite.sdk.internal;

import android.webkit.JavascriptInterface;

import androidx.annotation.Nullable;


public class StoreJsInterfaceImpl {
    @Nullable
    private StoreDelegate store;

    public StoreJsInterfaceImpl() {
    }

    public void setStore(StoreDelegate store) {
        this.store = store;
    }

    @JavascriptInterface
    public void fireSyncedStoreUpdate(Object args) {
        try {
            if (store != null) {
                store.fireSyncedStoreUpdate(String.valueOf(args));
            }
        } catch (Exception e) {
            Logger.error("fireSyncedStoreUpdate", e);
        }
    }
}
