package com.herewhite.sdk.domain;

import java.util.HashMap;
import java.util.Map;

public class RoomMouseEvent extends WhiteObject {
    private double x;
    private double y;
    private Map<Object,Object> targetsMap = new HashMap<>();


    public RoomMouseEvent(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Map<Object, Object> getTargetsMap() {
        return targetsMap;
    }

    public void setTargetsMap(Map<Object, Object> targetsMap) {
        this.targetsMap = targetsMap;
    }
}
