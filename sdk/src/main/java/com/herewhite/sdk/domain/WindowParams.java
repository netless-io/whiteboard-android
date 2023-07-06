package com.herewhite.sdk.domain;

import java.util.HashMap;

public class WindowParams extends WhiteObject {
    /**
     * 各个端本地显示多窗口内容时，高与宽比例，默认为 9:16
     */
    private Float containerSizeRatio;
    /**
     * 多窗口区域（主窗口）以外的空间显示 PS 棋盘背景，默认 true
     */
    private Boolean chessboard;
    /**
     * 驼峰形式的 CSS，透传给多窗口时，最小化 div 的 css
     */
    private HashMap<String, String> collectorStyles;
    /**
     * 窗口样式覆盖
     */
    private String overwriteStyles;
    /**
     * 是否在网页控制台打印日志
     */
    private Boolean debug;

    /**
     * 窗口配色模式
     */
    private WindowPrefersColorScheme prefersColorScheme;

    /**
     * 是否全屏
     */
    private Boolean fullscreen;

    public Float getContainerSizeRatio() {
        return containerSizeRatio;
    }

    public WindowParams setContainerSizeRatio(Float containerSizeRatio) {
        this.containerSizeRatio = containerSizeRatio;
        return this;
    }

    public Boolean getChessboard() {
        return chessboard;
    }

    public WindowParams setChessboard(Boolean chessboard) {
        this.chessboard = chessboard;
        return this;
    }

    public Boolean getDebug() {
        return debug;
    }

    public WindowParams setDebug(Boolean debug) {
        this.debug = debug;
        return this;
    }

    public HashMap<String, String> getCollectorStyles() {
        return collectorStyles;
    }

    public WindowParams setCollectorStyles(HashMap<String, String> collectorStyles) {
        this.collectorStyles = collectorStyles;
        return this;
    }

    public String getOverwriteStyles() {
        return overwriteStyles;
    }

    public WindowParams setOverwriteStyles(String overwriteStyles) {
        this.overwriteStyles = overwriteStyles;
        return this;
    }

    public WindowPrefersColorScheme getPrefersColorScheme() {
        return prefersColorScheme;
    }

    public void setPrefersColorScheme(WindowPrefersColorScheme prefersColorScheme) {
        this.prefersColorScheme = prefersColorScheme;
    }

    public Boolean getFullscreen() {
        return fullscreen;
    }

    public WindowParams setFullscreen(Boolean fullscreen) {
        this.fullscreen = fullscreen;
        return this;
    }
}
