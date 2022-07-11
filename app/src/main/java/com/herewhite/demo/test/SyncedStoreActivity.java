package com.herewhite.demo.test;

import android.view.View;

import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivitySyncedStoreBinding;
import com.herewhite.sdk.SyncedStore;
import com.herewhite.sdk.SyncedStoreObject;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;

import java.util.Arrays;

public class SyncedStoreActivity extends SampleBaseActivity {
    static final String TAG = SyncedStoreActivity.class.getSimpleName();
    static final String MAIN_STORAGE_NAME = "main";

    ActivitySyncedStoreBinding binding;

    private SyncedStore syncedStore;
    private MainStorage mainStorage;

    static class MainStorage extends SyncedStoreObject {
        public Integer intValue;
        public String strValue;
        public ObjValue objValue;
        public Obj2Value obj2Value;

        public MainStorage() {
        }

        public MainStorage(Integer intValue, String strValue) {
            this.intValue = intValue;
            this.strValue = strValue;
        }

        public static class ObjValue {
            String strValue;
        }

        public static class Obj2Value {
            ObjValue objValue;
        }
    }

    @Override
    protected View getContentView() {
        binding = ActivitySyncedStoreBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected WhiteSdkConfiguration generateSdkConfig() {
        WhiteSdkConfiguration configuration = super.generateSdkConfig();
        configuration.setEnableSyncedStore(true);
        return configuration;
    }

    @Override
    protected void initView() {
        binding.connectStorage.setOnClickListener(v -> {

        });
        binding.getStorageState.setOnClickListener(v -> {
            logRoomInfo("storage main sync state" + syncedStore.getStorageState(MAIN_STORAGE_NAME).toString());
        });
        binding.getStorageStateAsync.setOnClickListener(v -> {
            syncedStore.getStorageState(MAIN_STORAGE_NAME, new Promise<SyncedStoreObject>() {
                @Override
                public void then(SyncedStoreObject syncedStoreObject) {
                    logRoomInfo("storage main async state" + syncedStoreObject);
                }

                @Override
                public void catchEx(SDKError t) {

                }
            });
        });
        binding.setStorageState.setOnClickListener(new View.OnClickListener() {
            int index = 0;
            MainStorage[] updates = new MainStorage[4];

            {
                updates[0] = new MainStorage();
                updates[0].intValue = 1;

                updates[1] = new MainStorage();
                updates[1].objValue = new MainStorage.ObjValue();
                updates[1].objValue.strValue = "native update obj";

                updates[2] = new MainStorage();
                updates[2].obj2Value = new MainStorage.Obj2Value();
                updates[2].obj2Value.objValue = new MainStorage.ObjValue();
                updates[2].obj2Value.objValue.strValue = "native update obj2";

                updates[3] = new MainStorage();
            }


            @Override
            public void onClick(View v) {
                if (updates[3] == null) {
                    updates[3] = mainStorage;
                }
                if (syncedStore != null) {
                    syncedStore.setStorageState(MAIN_STORAGE_NAME, updates[index++]);
                    index = index % updates.length;
                }
            }
        });
        binding.emptyStorage.setOnClickListener(v -> {
            syncedStore.emptyStorage(MAIN_STORAGE_NAME);
        });
        binding.destroyStorage.setOnClickListener(v -> {
            syncedStore.destroyStorage(MAIN_STORAGE_NAME);
        });
    }

    @Override
    protected void onJoinRoomSuccess() {
        syncedStore = room.obtainSyncedStore();
        syncedStore.addOnStateChangedListener(MAIN_STORAGE_NAME, (diff, currentValue) -> {
            logRoomInfo("storage[main] updated" + "\tdiff:" + diff.toString() + "\tvalue:" + currentValue.toString());
        });
        syncedStore.connectStorage(MAIN_STORAGE_NAME, new MainStorage(), new Promise<MainStorage>() {
            @Override
            public void then(MainStorage mainSyncedStore) {
                SyncedStoreActivity.this.mainStorage = mainSyncedStore;
            }

            @Override
            public void catchEx(SDKError t) {
                alert("connectStorage error", Arrays.toString(t.getStackTrace()));
            }
        });
    }
}
