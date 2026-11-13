package com.herewhite.sdk;

import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import wendu.dsbridge.special.OnReturnValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

public class RoomApiReturnTest {
    private static final String FAILURE = "{\"__error\":{\"message\":\"synthetic failure\",\"jsStack\":\"synthetic stack\"}}";
    private static class Result<T> implements Promise<T> {
        T value; SDKError error; int calls;
        public void then(T value) { this.value = value; calls++; }
        public void catchEx(SDKError error) { this.error = error; calls++; }
    }
    @SuppressWarnings("unchecked")
    private Result<?> invoke(String api, Object value) {
        JsBridgeInterface bridge = mock(JsBridgeInterface.class);
        Room room = new Room("synthetic-room", bridge, 160, false);
        Result result = new Result();
        boolean noArgs = api.equals("nextPage") || api.equals("prevPage");
        if (api.equals("nextPage")) room.nextPage(result);
        else if (api.equals("prevPage")) room.prevPage(result);
        else if (api.equals("closeApp")) room.closeApp("app-id", result);
        else room.insertText(0, 0, "text", result);
        ArgumentCaptor<OnReturnValue> callback = ArgumentCaptor.forClass(OnReturnValue.class);
        if (noArgs) verify(bridge).callHandler(eq("room." + api), callback.capture());
        else verify(bridge).callHandler(eq("room." + api), any(Object[].class), callback.capture());
        callback.getValue().onValue(value);
        assertEquals(1, result.calls);
        return result;
    }
    @Test public void errorsDoNotBecomeSuccessValues() {
        for (String api : new String[]{"nextPage", "prevPage", "closeApp", "insertText"}) {
            Result<?> result = invoke(api, FAILURE);
            assertNotNull(api, result.error); assertEquals("synthetic failure", result.error.getMessage());
            assertNull(result.value);
        }
    }
    @Test public void preservesNormalWireValues() {
        assertEquals(Boolean.FALSE, invoke("nextPage", false).value);
        assertEquals(Boolean.TRUE, invoke("prevPage", true).value);
        assertEquals(Boolean.TRUE, invoke("closeApp", null).value);
        assertEquals("text-id", invoke("insertText", "text-id").value);
    }
    @Test public void wrongTypesRejectRatherThanCastOrSucceed() {
        assertNotNull(invoke("nextPage", "false").error);
        assertNotNull(invoke("prevPage", null).error);
        assertNotNull(invoke("closeApp", true).error);
        assertNotNull(invoke("insertText", null).error);
    }
}
