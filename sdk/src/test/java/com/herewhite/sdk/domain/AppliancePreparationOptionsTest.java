package com.herewhite.sdk.domain;

import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdkConfiguration;
import org.json.JSONObject;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class AppliancePreparationOptionsTest {
    @Test public void sdkAndRoomBothSerializeExplicitExtrasWithoutInventingDefaults() throws Exception {
        WhiteSdkConfiguration sdk = new WhiteSdkConfiguration("app-id");
        assertFalse(sdk.toJSON().has("appliancePluginOptions"));
        AppliancePluginOptions options = new AppliancePluginOptions();
        Map<String,Object> extras = new HashMap<>();
        extras.put("useWorker", "auto"); extras.put("useSimple", false); extras.put("bufferSize", 0);
        options.setExtras(extras); sdk.setAppliancePluginOptions(options);
        JSONObject json = sdk.toJSON().getJSONObject("appliancePluginOptions").getJSONObject("extras");
        assertEquals("auto", json.getString("useWorker"));
        assertFalse(json.getBoolean("useSimple")); assertEquals(0, json.getInt("bufferSize"));
        RoomParams room = new RoomParams("uuid", "token", "uid");
        assertFalse(room.toJSON().has("appliancePluginOptions"));
        room.setAppliancePluginOptions(options);
        assertEquals(json.toString(), room.toJSON().getJSONObject("appliancePluginOptions").getJSONObject("extras").toString());
        assertFalse(sdk.toJSON().getJSONArray("__nativeConfigDefaults").toString().contains("appliancePluginOptions"));
    }
}
