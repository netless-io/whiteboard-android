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
    private MainSyncedStore mainSyncedStore;

    static class MainSyncedStore extends SyncedStoreObject {
        public Integer intValue;
        public String strValue;
        public ObjValue objValue;
        public Obj2Value obj2Value;

        public MainSyncedStore() {
        }

        public MainSyncedStore(Integer intValue, String strValue) {
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

        });
        binding.getStorageStateAsync.setOnClickListener(v -> {

        });
        binding.setStorageState.setOnClickListener(new View.OnClickListener() {
            int index = 0;
            MainSyncedStore[] updates = new MainSyncedStore[4];

            {
                updates[0] = new MainSyncedStore();
                updates[0].intValue = 1;

                updates[1] = new MainSyncedStore();
                updates[1].objValue = new MainSyncedStore.ObjValue();
                updates[1].objValue.strValue = "native update obj";

                updates[2] = new MainSyncedStore();
                updates[2].obj2Value = new MainSyncedStore.Obj2Value();
                updates[2].obj2Value.objValue = new MainSyncedStore.ObjValue();
                updates[2].obj2Value.objValue.strValue = "native update obj2";

                updates[3] = new MainSyncedStore();
            }


            @Override
            public void onClick(View v) {
                if (updates[3] == null) {
                    updates[3] = mainSyncedStore;
                }
                if (syncedStore != null) {
                    syncedStore.setStorageState(MAIN_STORAGE_NAME, updates[index++]);
                    index = index % updates.length;
                }
            }
        });
        binding.emptyStorage.setOnClickListener(v -> {

        });
    }

    @Override
    protected void onJoinRoomSuccess() {
        syncedStore = room.obtainSyncedStore();
        syncedStore.connectStorage(MAIN_STORAGE_NAME, new MainSyncedStore(), new Promise<MainSyncedStore>() {
            @Override
            public void then(MainSyncedStore mainSyncedStore) {
                SyncedStoreActivity.this.mainSyncedStore = mainSyncedStore;
            }

            @Override
            public void catchEx(SDKError t) {
                alert("connectStorage error", Arrays.toString(t.getStackTrace()));
            }
        });
        syncedStore.setOnStateChangedListener(MAIN_STORAGE_NAME, (diff, currentValue) -> {
            logRoomInfo("syncedStore main updated" + "\tdiff:" + diff.toString() + "\tvalue:" + currentValue.toString());
        });
    }
}
