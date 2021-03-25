package com.herewhite.demo;

import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;

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
import static org.junit.Assert.assertEquals;

public class PlayerTest {
    private IdlingResource mIdlingResource;
    private PlayActivity mActivity;

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule(PlayActivity.class) {
        @Override
        protected void afterActivityLaunched() {
            mActivity = (PlayActivity) activityRule.getActivity();

            mIdlingResource = mActivity.getIdlingResource();
            IdlingRegistry.getInstance().register(mIdlingResource);
        }
    };

    @Test
    public void testSetAndGet_PlaybackSpeed() {
        onView(withId(R.id.white)).check(matches(isDisplayed()));
        // 参数化测试支持不理想
        final double[] speeds = new double[]{1.25f, 1.5f, 2.0f};
        for (double speedTarget : speeds) {
            mActivity.mPlaybackPlayer.setPlaybackSpeed(speedTarget);
            assertEquals(speedTarget, mActivity.mPlaybackPlayer.getPlaybackSpeed(), Constants.DOUBLE_DELTA);
            mActivity.mPlaybackPlayer.getPlaybackSpeed(new Promise<Double>() {
                @Override
                public void then(Double speed) {
                    assertEquals(speed, speed, Constants.DOUBLE_DELTA);
                }

                @Override
                public void catchEx(SDKError t) {

                }
            });
        }
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }
}
