package com.herewhite.sdk.window;

import com.herewhite.sdk.domain.UnifiedPageStateChange;

/** Receives unified page and scale observations and command failures. */
public interface UnifiedPageStateListener {
    default void onUnifiedPageStateChange(UnifiedPageStateChange state) {}
}
