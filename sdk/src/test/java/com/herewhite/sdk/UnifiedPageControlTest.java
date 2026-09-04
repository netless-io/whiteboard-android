package com.herewhite.sdk;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.DispatchDocsEventResult;
import com.herewhite.sdk.domain.UnifiedPageStateChange;
import com.herewhite.sdk.domain.WindowDocsEvent;
import com.herewhite.sdk.domain.WindowPageStateOptions;
import com.herewhite.sdk.internal.SdkJsInterfaceImpl;
import com.herewhite.sdk.window.UnifiedPageStateListener;

import org.json.JSONObject;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class UnifiedPageControlTest {
    @Test
    public void serializesUnifiedPageOptions() {
        WindowDocsEvent.Options options = new WindowDocsEvent.Options();
        options.setTarget("Slide-1");
        options.setPage(2);
        options.setScale(1.5);

        Object[] values = Utils.toBridgeMaps(new Object[]{"scalePage", options});
        assertEquals("scalePage", values[0]);
        assertTrue(values[1] instanceof JSONObject);
        JSONObject json = (JSONObject) values[1];
        assertEquals("Slide-1", json.optString("target"));
        assertEquals(2, json.optInt("page"));
        assertEquals(1.5, json.optDouble("scale"), 0.0);
    }

    @Test
    public void serializesPageStateTargetWithoutCommandFields() {
        WindowPageStateOptions options = new WindowPageStateOptions().withTarget("Presentation-1");
        Object[] values = Utils.toBridgeMaps(new Object[]{options});
        JSONObject json = (JSONObject) values[0];
        assertEquals("Presentation-1", json.optString("target"));
        assertTrue(!json.has("page"));
        assertTrue(!json.has("scale"));
    }

    @Test
    public void staticPageEventsCreateIndependentImmutableEvents() {
        WindowDocsEvent targetedEvent = WindowDocsEvent.NextPage.withTarget("mainView");
        WindowDocsEvent appEvent = targetedEvent.withTarget("DocsViewer-1");
        assertEquals("mainView", targetedEvent.getOptions().getTarget());
        assertEquals("DocsViewer-1", appEvent.getOptions().getTarget());
        assertNull(WindowDocsEvent.NextPage.getOptions().getTarget());
    }

    @Test
    public void parsesDocsViewerUnsupportedScaleResult() {
        DispatchDocsEventResult result = new Gson().fromJson(
                "{\"accepted\":false,\"reason\":\"eventNotSupported\","
                        + "\"message\":\"DocsViewer does not support scalePage\"}",
                DispatchDocsEventResult.class);
        assertTrue(!result.isAccepted());
        assertEquals(DispatchDocsEventResult.EVENT_NOT_SUPPORTED, result.getReason());
        assertEquals("DocsViewer does not support scalePage", result.getMessage());
    }

    @Test
    public void forwardsUnifiedPageScaleAndFailurePayloads() {
        SdkJsInterfaceImpl jsInterface = new SdkJsInterfaceImpl(null, mock(WhiteSdk.class));
        AtomicReference<UnifiedPageStateChange> state = new AtomicReference<>();
        jsInterface.setUnifiedPageStateListener(new UnifiedPageStateListener() {
            @Override
            public void onUnifiedPageStateChange(UnifiedPageStateChange value) { state.set(value); }
        });

        jsInterface.unifiedPageStateChange("{\"target\":\"DocsViewer\",\"appId\":\"DocsViewer-1\","
                + "\"page\":2,\"pageCount\":3,\"scale\":1.25,\"status\":\"success\","
                + "\"changeType\":\"scale\"}");
        assertEquals("DocsViewer", state.get().getTarget());
        assertEquals("scale", state.get().getChangeType());
        assertEquals(Double.valueOf(1.25), state.get().getScale());

        jsInterface.unifiedPageStateChange("{\"target\":\"Slide\",\"appId\":\"Slide-1\","
                + "\"event\":\"nextPage\",\"page\":2,\"pageCount\":3,\"status\":\"failure\","
                + "\"reason\":\"commandFailed\",\"message\":\"render failed\"}");
        assertEquals("failure", state.get().getStatus());
        assertEquals("nextPage", state.get().getEvent());
        assertEquals("commandFailed", state.get().getReason());
        assertEquals("render failed", state.get().getMessage());
    }
}
