package com.herewhite.demo;

import android.view.View;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.matcher.ViewMatchers;

import static androidx.test.espresso.action.ViewActions.actionWithAssertions;

class TestUtils {
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ViewAction waitFor(long delay) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for " + delay + " milliseconds";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(delay);
            }
        };
    }

    public static ViewAction waitUntil(Matcher<View> matcher) {
        return actionWithAssertions(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                StringDescription description = new StringDescription();
                matcher.describeTo(description);
                return String.format("wait until: %s", description);
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (!matcher.matches(view)) {
                    LayoutChangeCallback callback = new LayoutChangeCallback(matcher);
                    try {
                        IdlingRegistry.getInstance().register(callback);
                        view.addOnLayoutChangeListener(callback);
                        uiController.loopMainThreadUntilIdle();
                    } finally {
                        view.removeOnLayoutChangeListener(callback);
                        IdlingRegistry.getInstance().unregister(callback);
                    }
                }
            }
        });
    }

    private static class LayoutChangeCallback implements IdlingResource, View.OnLayoutChangeListener {
        private Matcher<View> matcher;
        private IdlingResource.ResourceCallback callback;
        private boolean matched = false;

        LayoutChangeCallback(Matcher<View> matcher) {
            this.matcher = matcher;
        }

        @Override
        public String getName() {
            return "Layout change callback";
        }

        @Override
        public boolean isIdleNow() {
            return matched;
        }

        @Override
        public void registerIdleTransitionCallback(IdlingResource.ResourceCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            matched = matcher.matches(v);
            callback.onTransitionToIdle();
        }
    }

    public static ViewAction downToUp(float startX, float startY, float endX, float endY) {
        return new GeneralSwipeAction(
                Swipe.FAST,
                view -> {
                    final int[] xy = new int[2];
                    view.getLocationOnScreen(xy);
                    final float x = xy[0] + startX;
                    final float y = xy[1] + startY;
                    return new float[]{x, y};
                },
                view -> {
                    final int[] xy = new int[2];
                    view.getLocationOnScreen(xy);
                    final float x = xy[0] + endX;
                    final float y = xy[1] + endY;
                    return new float[]{x, y};
                },
                Press.FINGER
        );
    }
}
