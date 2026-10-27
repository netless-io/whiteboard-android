package com.herewhite.sdk;

import com.herewhite.sdk.domain.AppliancePluginOptions;
import com.herewhite.sdk.domain.Region;
import com.herewhite.sdk.domain.WindowParams;
import org.json.JSONObject;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/** Check the actual Native copy/bridge conversion, rather than only the Java field types. */
public class ApplianceWorkerOptionsTest {
    @Test public void preservesWorkerModesAndBooleanTypesAcrossBridgeSerialization() throws Exception {
        for (String mode : new String[]{"simple", "dual", "mainThread"}) {
            RoomParams params = new RoomParams("uuid", "test-token", "uid");
            params.setRegion(Region.cn);
            params.setWritable(false);
            params.setWindowParams(new WindowParams());
            Map<String, Object> extras = new LinkedHashMap<>();
            extras.put("useWorker", mode.equals("mainThread") ? "mainThread" : "auto");
            extras.put("useSimple", !mode.equals("dual"));
            extras.put("allowImageBitmapFallback", true);
            AppliancePluginOptions options = new AppliancePluginOptions();
            options.setExtras(extras);
            params.setAppliancePluginOptions(options);

            RoomParams copy = Utils.deepCopy(params, RoomParams.class);
            extras.put("useSimple", "mutated-after-copy");
            Object converted = Utils.toBridgeMaps(new Object[]{copy})[0];
            assertTrue(converted instanceof JSONObject);
            JSONObject json = new JSONObject(converted.toString());
            assertEquals("uuid", json.getString("uuid"));
            assertEquals("test-token", json.getString("roomToken"));
            assertEquals("cn-hz", json.getString("region"));
            assertSame(Boolean.FALSE, json.get("isWritable"));
            assertTrue(json.has("windowParams"));
            JSONObject plugin = json.getJSONObject("appliancePluginOptions");
            assertFalse(plugin.has("cdn")); // Leave bundled source selection to Bridge.
            JSONObject flags = plugin.getJSONObject("extras");
            assertEquals(mode.equals("mainThread") ? "mainThread" : "auto", flags.get("useWorker"));
            assertEquals(!mode.equals("dual"), flags.get("useSimple"));
            assertSame(Boolean.TRUE, flags.get("allowImageBitmapFallback"));
        }
    }

    @Test public void unconfiguredRoomDoesNotForceWorkerOptions() throws Exception {
        JSONObject json = (JSONObject) Utils.toBridgeMaps(new Object[]{
                Utils.deepCopy(new RoomParams("uuid", "test-token", "uid"), RoomParams.class)
        })[0];
        assertFalse(json.has("appliancePluginOptions"));
    }
}
