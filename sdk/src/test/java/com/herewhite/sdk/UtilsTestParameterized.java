package com.herewhite.sdk;

import com.herewhite.sdk.domain.WhiteObject;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * 用于测试接口转化函数
 */
@RunWith(Parameterized.class)
public class UtilsTestParameterized extends TestCase {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, "[]"},
                {new Object[]{-1, -1.797e+40}, "[-1,-1.797E40]"},
                {testFull, "[123,123,92233720368547,92233720368547,123,123,123,123,true,1,1.797E40,\"a\",\"a\",{\"value\":\"123\"},\"[{\\\"value\\\":\\\"123\\\"},{\\\"value\\\":\\\"234\\\"}]\"]"},
        });
    }

    static Object[] testFull = new Object[]{
            123,
            Integer.valueOf(123),
            92233720368547L,
            Long.valueOf(92233720368547L),
            (short) 123,
            Short.valueOf((short) 123),
            (byte) 123,
            Byte.valueOf((byte) 123),
            true,
            1.0f,
            1.797e+40,
            'a',
            new Character('a'),
            new LocalWhiteObject("123"),
            Arrays.asList(new LocalWhiteObject("123"), new LocalWhiteObject("234")),
    };

    static class LocalWhiteObject extends WhiteObject {
        private final String value;

        public LocalWhiteObject(String value) {
            this.value = value;
        }
    }

    @Test
    public void testToBridgeMaps_callNull_returnZeroArray() {
        Object[] converted = Utils.toBridgeMaps(null);
        assertTrue(converted != null && converted.length == 0);
    }

    @Parameterized.Parameter(0)
    public /* NOT private */ Object[] fInput;

    @Parameterized.Parameter(1)
    public /* NOT private */ String fExpected;

    @Test
    public void testToBridgeMaps() {
        Object[] converted = Utils.toBridgeMaps(fInput);
        // From DWebView
        String actual = new JSONArray(Arrays.asList(converted)).toString();
        assertEquals(fExpected, actual);
    }
}