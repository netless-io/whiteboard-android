package com.herewhite.sdk.internal;
import com.herewhite.sdk.LifecycleHost;

import android.webkit.JavascriptInterface;

import androidx.annotation.Nullable;


public class StoreJsInterfaceImpl {
    private LifecycleHost lifecycle;
    @Nullable
    private volatile StoreDelegate store;

    public StoreJsInterfaceImpl() {}
    public StoreJsInterfaceImpl(LifecycleHost lifecycle) { this.lifecycle = lifecycle; }

    public void setStore(StoreDelegate store) {
        this.store = store;
    }

    @JavascriptInterface
    public void fireSyncedStoreUpdate(Object input) {
        final StoreDelegate destination = store;
        Object scopedArgs = input;
        if (lifecycle != null) { scopedArgs = lifecycle.unwrap(input, true); if (scopedArgs == LifecycleHost.REJECTED) { return; } }
        final Object args = scopedArgs;
        try {
            if (destination != null) {
                destination.fireSyncedStoreUpdate(String.valueOf(args));
            }
        } catch (Exception e) {
            Logger.error("fireSyncedStoreUpdate", e);
        }
    }
}
