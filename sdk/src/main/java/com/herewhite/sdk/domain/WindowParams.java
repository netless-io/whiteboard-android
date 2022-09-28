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
     * 多窗口全屏模式
     */
    private TeleBoxFullscreen fullscreen;

    /**
     * 窗口配色模式
     */
    private WindowPrefersColorScheme prefersColorScheme;

    /**
     * Custom styles for telebox manager container
     */
    private String containerStyle;

    /**
     * Custom styles for telebox manager stage
     */
    private String stageStyle;

    /**
     * 自定义多窗口区域（主窗口）样式
     */
    private String defaultBoxBodyStyle;

    /**
     * 自定义独立窗口样式
     */
    private String defaultBoxStageStyle;

    /**
     * 多窗口主题配置
     */
    private TeleBoxManagerThemeConfig theme;

    /**
     * 是否只允许垂直滚动。（默认为 FALSE)
     * TRUE:  只允许垂直滚动，不允许放大。
     * FALSE: 允许放大和任意方向滚动。
     * 注意该值必须在各端保持一致，否则会导致画布无法同步。该参数为 TURE 时，与 room.viewMode 冲突。
     */
    private Boolean scrollVerticalOnly = false;

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

    public WindowParams setPrefersColorScheme(WindowPrefersColorScheme prefersColorScheme) {
        this.prefersColorScheme = prefersColorScheme;
        return this;
    }

    public TeleBoxFullscreen getFullscreen() {
        return fullscreen;
    }

    public WindowParams setFullscreen(TeleBoxFullscreen fullscreen) {
        this.fullscreen = fullscreen;
        return this;
    }

    public String getContainerStyle() {
        return containerStyle;
    }

    public WindowParams setContainerStyle(String containerStyle) {
        this.containerStyle = containerStyle;
        return this;
    }

    public String getStageStyle() {
        return stageStyle;
    }

    public WindowParams setStageStyle(String stageStyle) {
        this.stageStyle = stageStyle;
        return this;
    }

    public String getDefaultBoxBodyStyle() {
        return defaultBoxBodyStyle;
    }

    public WindowParams setDefaultBoxBodyStyle(String defaultBoxBodyStyle) {
        this.defaultBoxBodyStyle = defaultBoxBodyStyle;
        return this;
    }

    public String getDefaultBoxStageStyle() {
        return defaultBoxStageStyle;
    }

    public WindowParams setDefaultBoxStageStyle(String defaultBoxStageStyle) {
        this.defaultBoxStageStyle = defaultBoxStageStyle;
        return this;
    }

    public TeleBoxManagerThemeConfig getTheme() {
        return theme;
    }

    public WindowParams setTheme(TeleBoxManagerThemeConfig theme) {
        this.theme = theme;
        return this;
    }

    public Boolean getScrollVerticalOnly() {
        return scrollVerticalOnly;
    }

    public void setScrollVerticalOnly(Boolean scrollVerticalOnly) {
        this.scrollVerticalOnly = scrollVerticalOnly;
    }
}
