package com.herewhite.sdk.domain;

import com.herewhite.sdk.converter.ConvertType;

/**
 * 内部类，文档中隐藏
 */
public class ConvertedFiles {
    private String taskId;
    private ConvertType type;
    private Double width;
    private Double height;
    private String[] slideURLs;
    private Scene[] scenes;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public ConvertType getType() {
        return type;
    }

    public void setType(ConvertType type) {
        this.type = type;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public String[] getSlideURLs() {
        return slideURLs;
    }

    public void setSlideURLs(String[] slideURLs) {
        this.slideURLs = slideURLs;
    }

    public Scene[] getScenes() {
        return scenes;
    }

    public void setScenes(Scene[] scenes) {
        this.scenes = scenes;
    }
}
