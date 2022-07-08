package com.herewhite.sdk;

import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.junit.Test;

public class SyncedStoreTest {

    static class TestSyncedStore extends SyncedStoreObject {
        String strValue;
        Integer intValue;
        ObjValue objValue;
        Obj2Value obj2Value;

        public static class ObjValue {
            String strValue;
        }

        public static class Obj2Value {
            ObjValue objValue;
        }
    }

    String jsonOld = "{\"intValue\":1,\"strValue\":\"1\",\"objValue\":{\"strValue\":\"2\"},\"obj2Value\":{\"objValue\":{\"strValue\":\"3\"}}}";
    String[] jsonMerges = new String[]{
            "{\"intValue\":2,\"strValue\":\"3\"}",
            "{\"objValue\":{\"strValue\":\"33\"}}",
            "{\"obj2Value\":{\"objValue\":{\"strValue\":\"123\"}}}",
    };
    String[] expectedJsons = new String[]{
            "{\"intValue\":2,\"strValue\":\"3\",\"objValue\":{\"strValue\":\"2\"},\"obj2Value\":{\"objValue\":{\"strValue\":\"3\"}}}",
            "{\"intValue\":1,\"strValue\":\"1\",\"objValue\":{\"strValue\":\"33\"},\"obj2Value\":{\"objValue\":{\"strValue\":\"3\"}}}",
            "{\"intValue\":1,\"strValue\":\"1\",\"objValue\":{\"strValue\":\"2\"},\"obj2Value\":{\"objValue\":{\"strValue\":\"123\"}}}",
    };

    @Test
    public void assignElement() {
        Gson gson = new Gson();
        for (int i = 0; i < jsonMerges.length; i++) {
            JsonElement target = SyncedStore.assignElement(gson.fromJson(jsonOld, JsonElement.class), gson.fromJson(jsonMerges[i], JsonElement.class));
            JsonElement expected = gson.fromJson(expectedJsons[i], JsonElement.class);
            assertTrue(CommonTestTools.compareJson(target, expected));
        }
    }
}