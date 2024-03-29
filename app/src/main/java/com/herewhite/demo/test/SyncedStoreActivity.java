package com.herewhite.demo.test;

import android.view.View;

import com.herewhite.demo.common.SampleBaseActivity;
import com.herewhite.demo.databinding.ActivitySyncedStoreBinding;
import com.herewhite.sdk.SyncedStore;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WhiteObject;

import java.util.Arrays;

public class SyncedStoreActivity extends SampleBaseActivity {
    static final String TAG = SyncedStoreActivity.class.getSimpleName();
    static final String MAIN_STORAGE_NAME = "main";

    ActivitySyncedStoreBinding binding;

    private SyncedStore syncedStore;
    private MainStorage mainStorage;

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
            syncedStore.connectStorage(MAIN_STORAGE_NAME, "{}", new Promise<String>() {
                @Override
                public void then(String mainSyncedStore) {
                    SyncedStoreActivity.this.mainStorage = gson.fromJson(mainSyncedStore, MainStorage.class);
                }

                @Override
                public void catchEx(SDKError t) {
                    alert("connectStorage error", Arrays.toString(t.getStackTrace()));
                }
            });
        });
        binding.getStorageState.setOnClickListener(v -> {
            MainStorage mainStorage = gson.fromJson(syncedStore.getStorageState(MAIN_STORAGE_NAME), MainStorage.class);
            logRoomInfo("storage main sync state" + mainStorage);
        });
        binding.getStorageStateAsync.setOnClickListener(v -> {
            syncedStore.getStorageState(MAIN_STORAGE_NAME, new Promise<String>() {
                @Override
                public void then(String json) {
                    MainStorage mainStorage = gson.fromJson(json, MainStorage.class);
                    logRoomInfo("storage main async state" + mainStorage);
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
                    syncedStore.setStorageState(MAIN_STORAGE_NAME, gson.toJson(updates[index++]));
                    index = index % updates.length;
                }
            }
        });
        binding.disconnectStorage.setOnClickListener(v -> {
            syncedStore.disconnectStorage(MAIN_STORAGE_NAME);
        });
        binding.resetState.setOnClickListener(v -> {
            syncedStore.resetState(MAIN_STORAGE_NAME);
        });
    }

    @Override
    protected void onJoinRoomSuccess() {
        syncedStore = room.getSyncedStore();
        syncedStore.addOnStateChangedListener(MAIN_STORAGE_NAME, (diff, currentValue) -> {
            logRoomInfo("storage[main] updated" + "\tdiff:" + diff + "\tvalue:" + currentValue);
        });
        syncedStore.connectStorage(MAIN_STORAGE_NAME, "{}", new Promise<String>() {
            @Override
            public void then(String mainSyncedStore) {
                SyncedStoreActivity.this.mainStorage = gson.fromJson(mainSyncedStore, MainStorage.class);
            }

            @Override
            public void catchEx(SDKError t) {
                alert("connectStorage error", Arrays.toString(t.getStackTrace()));
            }
        });
    }

    static class MainStorage extends WhiteObject {
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
}
