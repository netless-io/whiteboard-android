package com.herewhite.sdk.domain;

/**
 * Created by buhe on 2018/8/11.
 */

public class MemberState extends WhiteObject {
    private String currentApplianceName;
    private int[] strokeColor;
    private Double strokeWidth;
    private Double textSize;

    public MemberState() {
    }

    public String getCurrentApplianceName() {
        return currentApplianceName;
    }

    public void setCurrentApplianceName(String currentApplianceName) {
        this.currentApplianceName = currentApplianceName;
    }

    public int[] getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int[] strokeColor) {
        this.strokeColor = strokeColor;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public double getTextSize() {
        return textSize;
    }

    public void setTextSize(double textSize) {
        this.textSize = textSize;
    }
}
