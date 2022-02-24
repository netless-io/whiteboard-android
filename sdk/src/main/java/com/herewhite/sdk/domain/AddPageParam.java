package com.herewhite.sdk.domain;

public class AddPageParam extends WhiteObject {
    private Boolean after;
    private Scene scene;

    public AddPageParam() {
        this(null, false);
    }

    public AddPageParam(Scene scene) {
        this(scene, false);
    }

    public AddPageParam(Scene scene, Boolean after) {
        this.scene = scene;
        this.after = after;
    }
}
