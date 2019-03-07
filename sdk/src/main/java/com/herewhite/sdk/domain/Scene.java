package com.herewhite.sdk.domain;

public class Scene {

    private String name;
    private String componentsCount;
    private PptPage pptPage;

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

    public PptPage getPptPage() {
        return pptPage;
    }

    public void setPptPage(PptPage pptPage) {
        this.pptPage = pptPage;
    }
}
