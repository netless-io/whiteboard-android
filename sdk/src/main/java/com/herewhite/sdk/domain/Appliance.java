package com.herewhite.sdk.domain;


// Created by buhe on 2018/8/18.

/**
 * 白板工具名称。
 */
public class Appliance {
    /**
     * 铅笔。
     */
    public final static String PENCIL = "pencil";
    /**
     * 选择工具。
     */
    public final static String SELECTOR = "selector";
    /**
     * 矩形工具。
     */
    public final static String RECTANGLE = "rectangle";
    /**
     * 椭圆工具。
     */
    public final static String ELLIPSE = "ellipse";
    /**
     * 橡皮工具。
     */
    public final static String ERASER = "eraser";
    /**
     * 文本输入框。
     */
    public final static String TEXT = "text";
    /**
     * 直线工具。
     */
    public final static String STRAIGHT = "straight";
    /**
     * 箭头工具。
     */
    public final static String ARROW = "arrow";
    /**
     * 抓手工具。
     */
    public final static String HAND = "hand";
    /**
     * 激光笔。
     */
    public final static String LASER_POINTER = "laserPointer";
    /**
     * 点选工具。目前主要用于 H5 课件。
     */
    public final static String CLICKER = "clicker";
    /**
     * 图形工具。
     */
    public final static String SHAPE = "shape";
    /**
     * 橡皮工具
     * 用于擦除局部铅笔笔迹的橡皮工具。该工具仅对 NewPencil 生效，使用前需要先设置 disableNewPencil 为 false
     */
    public final static String PENCIL_ERASER = "pencilEraser";

    /**
     * 激光铅笔
     * 该工具只在开启 WhiteSdkConfiguration.enableAppliancePlugin 后生效
     */
    public final static String LASER_PENCIL = "laserPen";
}
