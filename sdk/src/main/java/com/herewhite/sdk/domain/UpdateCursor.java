package com.herewhite.sdk.domain;

public class UpdateCursor {
    private CursorView[] appearSet;
    private CursorView[] disappearSet;
    private CursorView[] updateSet;

    public CursorView[] getAppearSet() {
        return appearSet;
    }

    public void setAppearSet(CursorView[] appearSet) {
        this.appearSet = appearSet;
    }

    public CursorView[] getDisappearSet() {
        return disappearSet;
    }

    public void setDisappearSet(CursorView[] disappearSet) {
        this.disappearSet = disappearSet;
    }

    public CursorView[] getUpdateSet() {
        return updateSet;
    }

    public void setUpdateSet(CursorView[] updateSet) {
        this.updateSet = updateSet;
    }
}
