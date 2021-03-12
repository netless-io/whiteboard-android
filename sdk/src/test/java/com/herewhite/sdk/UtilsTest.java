package com.herewhite.sdk;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {
    private static final String jsonString = "{\"b\":true,\"inter\":{\"s\":\"Str\",\"i\":101}}\n";

    static class TestObject {
        private boolean b;
        private Inter inter;

        static class Inter {
            private String s;
            private int i;
        }
    }

    public void testFromJson() {
        TestObject testObject = Utils.fromJson(jsonString, TestObject.class);

        assertEquals(true, testObject.b);
        assertNotNull(testObject.inter);
        assertEquals("Str", testObject.inter.s);
        assertEquals(101, testObject.inter.i);
    }

    public void testDeepCopy() {
        TestObject testObject = Utils.fromJson(jsonString, TestObject.class);
        TestObject copyObject = Utils.deepCopy(testObject, TestObject.class);

        assertEquals(testObject.b, copyObject.b);
        assertEquals(testObject.inter.s, copyObject.inter.s);
        assertEquals(testObject.inter.i, copyObject.inter.i);
    }

    public void testGetDensityDpi() {

    }
}