package com.herewhite.sdk;

import com.herewhite.sdk.domain.Promise;

import junit.framework.TestCase;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RoomTest extends TestCase {
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
        mRoom.setRoomCallbacks(mockRoomCallbacks);
    }

    public void tearDown() throws Exception {
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
    }

    public void testGetObserverId() {
    }

    public void testSetObserverId() {
    }

    public void testSetGlobalState() {
    }

    public void testSetMemberState() {
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
    }

    public void testUndo() {
    }

    public void testSetViewMode() {
    }

    public void testDisconnect() {
        Promise<Object> mockPromise = mock(Promise.class);
        mRoom.disconnect(mockPromise);

        assertTrue(mRoom.getDisconnectedBySelf());
        verify(mockJsBridgeInterface).callHandler(eq("room.disconnect"), eq(new Object[]{}), anyObject());
    }

    public void testInsertImage() {
    }

    public void testCompleteImageUpload() {
    }

    public void testTestInsertImage() {
    }

    public void testGetGlobalState() {
    }

    public void testTestGetGlobalState() {
    }

    public void testGetMemberState() {
    }

    public void testTestGetMemberState() {
    }

    public void testGetRoomMembers() {
    }

    public void testTestGetRoomMembers() {
    }

    public void testGetBroadcastState() {
    }

    public void testTestGetBroadcastState() {
    }

    public void testGetSceneState() {
    }

    public void testTestGetSceneState() {
    }

    public void testGetScenes() {
    }

    public void testTestGetScenes() {
    }

    public void testGetZoomScale() {
    }

    public void testTestGetZoomScale() {
    }

    public void testGetRoomPhase() {
    }

    public void testTestGetRoomPhase() {
    }

    public void testGetRoomState() {
    }

    public void testTestGetRoomState() {
    }

    public void testSetScenePath() {
    }

    public void testTestSetScenePath() {
    }

    public void testSetSceneIndex() {
    }

    public void testPutScenes() {
    }

    public void testMoveScene() {
    }

    public void testRemoveScenes() {
    }

    public void testCleanScene() {
    }

    public void testPptNextStep() {
    }

    public void testPptPreviousStep() {
    }

    public void testZoomChange() {
    }

    public void testDebugInfo() {
    }

    public void testDisableOperations() {
    }

    public void testTestSetWritable() {
    }

    public void testDisableEraseImage() {
    }

    public void testDisableCameraTransform() {
    }

    public void testDisableDeviceInputs() {
    }

    public void testSetTimeDelay() {
    }

    public void testGetTimeDelay() {
    }

    public void testFireMagixEvent() {
    }

    public void testFireHighFrequencyEvent() {
    }

    public void testDispatchMagixEvent() {
    }

    public void testSetRoomCallbacks() {
    }

    public void testOnDisplayerStateChanged() {
    }

    public void testFireCanUndoStepsUpdate() {
    }

    public void testOnCanRedoStepsUpdate() {
    }

    public void testFireKickedWithReason() {
    }

    public void testFireDisconnectWithError() {
    }

    public void testFireCatchErrorWhenAppendFrame() {
    }

    public void testFireRoomStateChanged() {
    }
}