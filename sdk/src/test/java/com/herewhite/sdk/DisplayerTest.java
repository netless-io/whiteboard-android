package com.herewhite.sdk;

import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.RectangleConfig;
import com.herewhite.sdk.domain.WhiteObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DisplayerTest {
    private static final String UUID = "7e7c8f007a4011eba97639a3a8d1dde1";
    private static final int densityDpi = 3;

    private JsBridgeInterface mockJsBridgeInterface;
    private Displayer displayer;


    @Before
    public void setUp() throws Exception {
        mockJsBridgeInterface = mock(JsBridgeInterface.class);
        displayer = new Displayer(UUID, mockJsBridgeInterface, densityDpi);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void postIframeMessage_String() {
        String randomStr = "kUicZt8hzFqFWoje89U2";
        displayer.postIframeMessage(randomStr);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.postMessage"), aryEq(new Object[]{randomStr}));
    }

    @Test
    public void postIframeMessage_Object() {
        WhiteObject object = new WhiteObject();
        displayer.postIframeMessage(object);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.postMessage"), aryEq(new Object[]{object}));
    }

    @Test
    public void getScenePathType() {
        String path = "valid/path";
        displayer.getScenePathType(path, null);
        verify(mockJsBridgeInterface).callHandler(
                Matchers.eq("displayer.scenePathType"),
                aryEq(new Object[]{path}),
                any());
    }

    @Test
    public void getEntireScenes() {
        displayer.getEntireScenes(null);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.entireScenes"), (OnReturnValue<Object>) any());
    }

    @Test
    public void refreshViewSize() {
        displayer.refreshViewSize();
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.refreshViewSize"), (Object[]) any());
    }

    @Test
    public void scalePptToFit() {
        displayer.scalePptToFit();
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.scalePptToFit"), (Object[]) any());
    }

    @Test
    public void testScalePptToFit() {
        AnimationMode mode = AnimationMode.Continuous;
        displayer.scalePptToFit(mode);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.scalePptToFit"), aryEq(new Object[]{"continuous"}));
    }

    @Test
    public void addMagixEventListener() {
        displayer.eventListenerMap = mock(ConcurrentHashMap.class);
        displayer.addMagixEventListener("eventName", null);

        verify(displayer.eventListenerMap).put(any(), any());
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.addMagixEventListener"), aryEq(new Object[]{"eventName"}));
    }

    @Test
    public void addHighFrequencyEventListener() {
        displayer.frequencyEventListenerMap = mock(ConcurrentHashMap.class);

        displayer.addHighFrequencyEventListener("eventName", null, 600);
        verify(displayer.frequencyEventListenerMap).put(any(), any());
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.addHighFrequencyEventListener"), aryEq(new Object[]{"eventName", 600}));
    }

    @Test
    public void addHighFrequencyEventListener_lessThan500_delivery500() {
        displayer.frequencyEventListenerMap = mock(ConcurrentHashMap.class);

        displayer.addHighFrequencyEventListener("eventName", null, 400);
        verify(displayer.frequencyEventListenerMap).put(any(), any());
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.addHighFrequencyEventListener"), aryEq(new Object[]{"eventName", 500}));
    }

    @Test
    public void removeMagixEventListener() {
        displayer.eventListenerMap = mock(ConcurrentHashMap.class);
        displayer.removeMagixEventListener("eventName");

        verify(displayer.eventListenerMap).remove("eventName");
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.removeMagixEventListener"), aryEq(new Object[]{"eventName"}));
    }

    @Test
    public void convertToPointInWorld() {
        displayer.convertToPointInWorld(100.0, 123.0, null);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.convertToPointInWorld"), aryEq(new Object[]{100.0, 123.0}), any());
    }

    @Test
    public void setCameraBound() {
        CameraBound cameraBound = new CameraBound();
        displayer.setCameraBound(cameraBound);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.setCameraBound"), aryEq(new Object[]{cameraBound}));
    }

    @Test
    public void getScenePreviewImage() {
        String scenePath = "/dir/path";
        displayer.getScenePreviewImage(scenePath, null);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayerAsync.scenePreview"), aryEq(new Object[]{scenePath}), any());
    }

    @Test
    public void getSceneSnapshotImage() {
        String scenePath = "/dir/path";
        displayer.getSceneSnapshotImage(scenePath, null);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayerAsync.sceneSnapshot"), aryEq(new Object[]{scenePath}), any());
    }

    @Test
    public void disableCameraTransform() {
        displayer.disableCameraTransform(false);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.setDisableCameraTransform"), aryEq(new Object[]{false}));
    }

    @Test
    public void moveCamera() {
        CameraConfig config = new CameraConfig();
        displayer.moveCamera(config);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.moveCamera"), aryEq(new Object[]{config}));
    }

    @Test
    public void moveCameraToContainer() {
        RectangleConfig config = new RectangleConfig(400.0, 400.0);
        displayer.moveCameraToContainer(config);
        verify(mockJsBridgeInterface).callHandler(Matchers.eq("displayer.moveCameraToContain"), aryEq(new Object[]{config}));
    }
}