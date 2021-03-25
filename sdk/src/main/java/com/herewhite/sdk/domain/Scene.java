package com.herewhite.sdk.domain;

public class Scene extends WhiteObject {

    private String name;
    private Long componentsCount;
    private PptPage ppt;

    /**
     * sdk 插入新场景时，会随机命名
     */
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

    public Long getComponentsCount() {
        return componentsCount;
    }

    public PptPage getPpt() {
        return ppt;
    }

    public void setPpt(PptPage ppt) {
        this.ppt = ppt;
    }
}
