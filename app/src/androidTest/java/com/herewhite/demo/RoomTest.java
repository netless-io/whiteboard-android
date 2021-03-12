package com.herewhite.demo;

import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.WhiteDisplayerState;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoomTest {
    private IdlingResource mIdlingResource;
    private RoomActivity mActivity;

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule(RoomActivity.class) {
        @Override
        protected void afterActivityLaunched() {
            mActivity = (RoomActivity) activityRule.getActivity();

            mIdlingResource = mActivity.getIdlingResource();
            IdlingRegistry.getInstance().register(mIdlingResource);
        }
    };

    @Test
    public void testSetAndGet_MemberState() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        MemberState state = new MemberState();
        state.setStrokeColor(new int[]{99, 99, 99});
        state.setCurrentApplianceName(Appliance.TEXT);
        state.setStrokeWidth(10);
        state.setTextSize(10);

        mActivity.mRoom.setMemberState(state);
        mActivity.mRoom.getMemberState(new Promise<MemberState>() {
            @Override
            public void then(MemberState memberState) {
                assertEquals(state.getCurrentApplianceName(), memberState.getCurrentApplianceName());
                assertArrayEquals(state.getStrokeColor(), memberState.getStrokeColor());
                assertEquals(state.getStrokeWidth(), memberState.getStrokeWidth(), Constants.DOUBLE_DELTA);
                assertEquals(state.getTextSize(), memberState.getTextSize(), Constants.DOUBLE_DELTA);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }

    class LocalGlobalState extends GlobalState {
        private String globalString = "globalString";
        private int globalInt = 0x1234;
    }

    @Test
    public void testSetAndGet_GlobalState() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));

        LocalGlobalState testState = new LocalGlobalState();
        mActivity.mRoom.setGlobalState(testState);

        WhiteDisplayerState.setCustomGlobalStateClass(LocalGlobalState.class);
        LocalGlobalState globalState = (LocalGlobalState) mActivity.mRoom.getGlobalState();
        assertTrue(globalState.globalInt == testState.globalInt);

        mActivity.mRoom.getGlobalState(new Promise<GlobalState>() {
            @Override
            public void then(GlobalState globalState) {
                LocalGlobalState globalState2 = (LocalGlobalState) mActivity.mRoom.getGlobalState();
                assertTrue(globalState2.globalInt == testState.globalInt);
                assertEquals(testState.globalString, globalState2.globalString);
            }

            @Override
            public void catchEx(SDKError t) {

            }
        });
    }


    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }
}