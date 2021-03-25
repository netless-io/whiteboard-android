package com.herewhite.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.herewhite.sdk.domain.AkkoEvent;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.ImageInformation;
import com.herewhite.sdk.domain.ImageInformationWithUrl;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.ViewMode;
import com.herewhite.sdk.util.GsonFieldIgnoreStrategy;

import junit.framework.TestCase;

import wendu.dsbridge.OnReturnValue;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RoomTest extends TestCase {
    Promise IGNORE_PROMISE = new Promise() {
        @Override
        public void then(Object o) {

        }

        @Override
        public void catchEx(SDKError t) {

        }
    };

    private static final String UUID = "7e7c8f007a4011eba97639a3a8d1dde1";
    private static final int densityDpi = 3;

    private JsBridgeInterface mockJsBridgeInterface;
    private RoomCallbacks mockRoomCallbacks;
    private Room mRoom;

    public void setUp() throws Exception {
        super.setUp();
        mockJsBridgeInterface = mock(JsBridgeInterface.class);
        mockRoomCallbacks = mock(RoomCallbacks.class);
        mRoom = new Room(UUID, mockJsBridgeInterface, densityDpi, false);
        mRoom.setRoomListener(mockRoomCallbacks);
    }

    public void tearDown() throws Exception {
    }

    private static final String ROOM_INITIAL_STATES = "{\"roomPhase\":\"connecting\",\"disconnectedBySelf\":false,\"writable\":null,\"timeDelay\":0,\"observerId\":null,\"localRoomStateListener\":{},\"roomDelegate\":null,\"backgroundColor\":-1,\"uuid\":\"7e7c8f007a4011eba97639a3a8d1dde1\",\"densityDpi\":3,\"eventListenerMap\":{},\"frequencyEventListenerMap\":{}}";

    public void testInitialStatesBySerialization() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .addSerializationExclusionStrategy(
                        new GsonFieldIgnoreStrategy("syncRoomState", "roomListener", "bridge", "handler")
                )
                .create();

        assertEquals(ROOM_INITIAL_STATES, gson.toJson(mRoom));
    }

    public void testSetAndGet_DisconnectedBySelf() {
        mRoom.setDisconnectedBySelf(false);
        assertTrue(mRoom.getDisconnectedBySelf() == false);
    }

    public void testSetAndGet_Writable() {
        mRoom.setWritable(true);
        assertTrue(mRoom.getWritable() == true);
    }

    public void testSetSyncRoomState() {
    }

    public void testSetRoomPhase() {
        mRoom.setRoomPhase(RoomPhase.connecting);
        assertEquals(RoomPhase.connecting, mRoom.getRoomPhase());
        verify(mockRoomCallbacks).onPhaseChanged(RoomPhase.connecting);
    }

    public void testSetGlobalState() {
        GlobalState globalState = new GlobalState();
        mRoom.setGlobalState(globalState);
        verify(mockJsBridgeInterface).callHandler("room.setGlobalState", new Object[]{globalState});
    }

    public void testSetMemberState() {
        MemberState state = new MemberState();
        mRoom.setMemberState(state);
        verify(mockJsBridgeInterface).callHandler("room.setMemberState", new Object[]{state});
    }

    public void testCopy() {
        mRoom.copy();
        verify(mockJsBridgeInterface).callHandler("room.sync.copy", new Object[]{});
    }

    public void testPaste() {
        mRoom.paste();
        verify(mockJsBridgeInterface).callHandler("room.sync.paste", new Object[]{});
    }

    public void testDuplicate() {
        mRoom.duplicate();
        verify(mockJsBridgeInterface).callHandler("room.sync.duplicate", new Object[]{});
    }

    public void testDeleteOperation() {
        mRoom.deleteOperation();
        verify(mockJsBridgeInterface).callHandler("room.sync.delete", new Object[]{});
    }

    public void testDisableSerialization() {
        mRoom.disableSerialization(false);
        verify(mockJsBridgeInterface).callHandler("room.sync.disableSerialization", new Object[]{false});
    }

    public void testRedo() {
        mRoom.redo();
        verify(mockJsBridgeInterface).callHandler("room.redo", new Object[]{});
    }

    public void testUndo() {
        mRoom.undo();
        verify(mockJsBridgeInterface).callHandler("room.undo", new Object[]{});
    }

    public void testSetViewMode() {
        mRoom.setViewMode(ViewMode.Freedom);
        verify(mockJsBridgeInterface).callHandler(eq("room.setViewMode"), aryEq(new Object[]{"Freedom"}));

        mRoom.setViewMode(ViewMode.Follower);
        verify(mockJsBridgeInterface).callHandler(eq("room.setViewMode"), aryEq(new Object[]{"Follower"}));

        mRoom.setViewMode(ViewMode.Broadcaster);
        verify(mockJsBridgeInterface).callHandler(eq("room.setViewMode"), aryEq(new Object[]{"Broadcaster"}));
    }

    public void testDisconnect() {
        Promise<Object> mockPromise = mock(Promise.class);
        mRoom.disconnect(mockPromise);

        assertTrue(mRoom.getDisconnectedBySelf());
        verify(mockJsBridgeInterface).callHandler(eq("room.disconnect"), eq(new Object[]{}), anyObject());
    }

    public void testInsertImage_ImageInformation() {
        ImageInformation imageInfo = new ImageInformation();
        mRoom.insertImage(imageInfo);
        verify(mockJsBridgeInterface).callHandler("room.insertImage", new Object[]{imageInfo});
    }

    public void testCompleteImageUpload() {
        mRoom.completeImageUpload("uuid", "url");
        verify(mockJsBridgeInterface).callHandler("room.completeImageUpload", new Object[]{"uuid", "url"});
    }

    public void testInsertImage_ImageInformationWithUrl() {
        ImageInformationWithUrl imageInfoWithUrl = new ImageInformationWithUrl(100d, 100d, 1080d, 720d, "url");
        mRoom.insertImage(imageInfoWithUrl);
        verify(mockJsBridgeInterface).callHandler(eq("room.insertImage"), (Object[]) any());
        verify(mockJsBridgeInterface).callHandler(eq("room.completeImageUpload"), (Object[]) any());
    }

    public void testGetGlobalState() {
        mRoom.getGlobalState(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.getGlobalState"), any(), any());
    }

    public void testGetMemberState() {
        mRoom.getMemberState(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.getMemberState"), (OnReturnValue<Object>) any());
    }

    public void testGetRoomMembers() {
        mRoom.getRoomMembers(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.getRoomMembers"), eq(new Object[]{}), any());
    }

    public void testGetBroadcastState() {
        mRoom.getBroadcastState(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.getBroadcastState"), (Object[]) any(), any());
    }

    public void testGetSceneState() {
        mRoom.getSceneState(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.getSceneState"), (Object[]) any(), any());
    }

    public void testGetScenes() {
        mRoom.getScenes(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.getScenes"), (Object[]) any(), any());
    }

    public void testGetZoomScale() {
        mRoom.getZoomScale(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.getZoomScale"), (OnReturnValue<Object>) any());
    }

    public void testGetRoomPhase() {
        mRoom.getRoomPhase(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.getRoomPhase"), (OnReturnValue<Object>) any());
    }

    public void testGetRoomState() {
        mRoom.getRoomState(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.state.getDisplayerState"), (OnReturnValue<Object>) any());
    }

    public void testSetScenePath() {
        mRoom.setScenePath("/dir/path");
        verify(mockJsBridgeInterface).callHandler("room.setScenePath", new Object[]{"/dir/path"});

        mRoom.setScenePath("/dir/path", IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.setScenePath"), eq(new Object[]{"/dir/path"}), (OnReturnValue<Object>) any());
    }

    public void testSetSceneIndex() {
        mRoom.setSceneIndex(1, IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.setSceneIndex"), eq(new Object[]{1}), (OnReturnValue<Object>) any());
    }

    public void testPutScenes() {
        Scene[] scenes = new Scene[]{
                new Scene("page1"),
                new Scene("page2")
        };
        mRoom.putScenes("/dir", scenes, 0);
        verify(mockJsBridgeInterface).callHandler("room.putScenes", new Object[]{"/dir", scenes, 0});
    }

    public void testMoveScene() {
        mRoom.moveScene("/dir/source", "/dir/target");
        verify(mockJsBridgeInterface).callHandler("room.moveScene", new Object[]{"/dir/source", "/dir/target"});
    }

    public void testRemoveScenes() {
        mRoom.removeScenes("/dir");
        verify(mockJsBridgeInterface).callHandler("room.removeScenes", new Object[]{"/dir"});
    }

    public void testCleanScene() {
        mRoom.cleanScene(true);
        verify(mockJsBridgeInterface).callHandler("room.cleanScene", new Object[]{true});
    }

    public void testPptNextStep() {
        mRoom.pptNextStep();
        verify(mockJsBridgeInterface).callHandler("ppt.nextStep", new Object[]{});
    }

    public void testPptPreviousStep() {
        mRoom.pptPreviousStep();
        verify(mockJsBridgeInterface).callHandler("ppt.previousStep", new Object[]{});
    }

    public void testZoomChange() {
        mRoom.zoomChange(2.0);
        verify(mockJsBridgeInterface).callHandler(eq("displayer.moveCamera"), (Object[]) any());
    }

    public void testDebugInfo() {
        mRoom.debugInfo(IGNORE_PROMISE);
        verify(mockJsBridgeInterface).callHandler(eq("room.state.debugInfo"), (OnReturnValue<Object>) any());
    }

    public void testDisableOperations() {
        mRoom.disableOperations(false);
        verify(mockJsBridgeInterface).callHandler("room.disableCameraTransform", new Object[]{false});
        verify(mockJsBridgeInterface).callHandler("room.disableDeviceInputs", new Object[]{false});
    }

    public void testDisableEraseImage() {
        mRoom.disableEraseImage(false);
        verify(mockJsBridgeInterface).callHandler("room.sync.disableEraseImage", new Object[]{false});
    }

    public void testDisableCameraTransform() {
        mRoom.disableCameraTransform(false);
        verify(mockJsBridgeInterface).callHandler("room.disableCameraTransform", new Object[]{false});
    }

    public void testDisableDeviceInputs() {
        mRoom.disableDeviceInputs(false);
        verify(mockJsBridgeInterface).callHandler("room.disableDeviceInputs", new Object[]{false});
    }

    public void testSetAndGetTimeDelay() {
        mRoom.setTimeDelay(1234);
        verify(mockJsBridgeInterface).callHandler("room.setTimeDelay", new Object[]{1234_000});
        assertEquals(Integer.valueOf(1234), mRoom.getTimeDelay());
    }

    public void testDispatchMagixEvent() {
        AkkoEvent akkoEvent = new AkkoEvent("eventName", "{}");
        mRoom.dispatchMagixEvent(akkoEvent);
        verify(mockJsBridgeInterface).callHandler("room.dispatchMagixEvent", new Object[]{akkoEvent});
    }
}