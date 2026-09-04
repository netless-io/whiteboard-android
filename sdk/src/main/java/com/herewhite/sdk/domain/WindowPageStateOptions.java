package com.herewhite.sdk.domain;

import androidx.annotation.Nullable;

/** Selects the mainView or a concrete DocsViewer, Slide, or Presentation app. */
public final class WindowPageStateOptions extends WhiteObject {
    @Nullable private final String target;

    public WindowPageStateOptions() {
        this(null);
    }

    private WindowPageStateOptions(@Nullable String target) {
        this.target = target;
    }

    @Nullable
    public String getTarget() {
        return target;
    }

    public WindowPageStateOptions withTarget(@Nullable String target) {
        return new WindowPageStateOptions(target);
    }
}
