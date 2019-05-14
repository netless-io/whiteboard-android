package com.herewhite.sdk.domain;

public class Scene {

    private String name;
    private String componentsCount;
    private PptPage ppt;

    public Scene() {
    }

    public Scene(String name) {
        this.name = name;
    }

    public Scene(String name, PptPage ppt) {
        this.name = name;
        this.ppt = ppt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentsCount() {
        return componentsCount;
    }

    public void setComponentsCount(String componentsCount) {
        this.componentsCount = componentsCount;
    }

    public PptPage getPpt() {
        return ppt;
    }

    public void setPpt(PptPage ppt) {
        this.ppt = ppt;
    }
}
