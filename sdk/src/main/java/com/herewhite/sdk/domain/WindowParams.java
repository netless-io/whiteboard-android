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

    private Boolean polling;

    /**
     * 是否使用每个窗口独立的 boxesStatus 状态管理。该参数需要在加入房间或创建回放前设置。
     */
    private Boolean useBoxesStatus;

    /**
     * 房间级强制最大化策略。可写端传 true 时会写入 attributes.forceMaximized 并把房间同步为最大化；
     * 所有支持该协议的客户端随后禁止进入 normal。
     * 需要 @netless/window-manager >= 1.0.23-beta.1。
     */
    private Boolean forceMaximized;

    /**
     * 最大化模式下仅初始化当前顶层 App runtime 的本地开关（不写入房间 attributes）。
     * 仅当房间已有 attributes.forceMaximized=true 且状态为 maximized/minimized 时生效，否则自动降级为 eager setup。
     * 需要 @netless/window-manager >= 1.0.23-beta.1。
     */
    private Boolean lazySetupInMaximizedMode;

    /**
     * lazy setup 模式下本地保留的 App runtime 最大数量，默认值由 WindowManager 决定。
     * 仅在 forceMaximized=true 且 lazySetupInMaximizedMode=true 时生效。
     * 需要 @netless/window-manager >= 1.0.23-beta.1。
     */
    private Integer maxCachedAppsInMaximizedMode;

    /** MainView reference size. Slide/Presentation receive originSize through addApp attributes. */
    private WindowOriginSize originSize;

    /** Optional relative scale bounds for dispatchDocsEvent scalePage. */
    private PageScaleRange pageScaleRange;

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

    public Boolean getPolling() {
        return polling;
    }

    public WindowParams setPolling(Boolean polling) {
        this.polling = polling;
        return this;
    }

    public Boolean getUseBoxesStatus() {
        return useBoxesStatus;
    }

    public WindowParams setUseBoxesStatus(Boolean useBoxesStatus) {
        this.useBoxesStatus = useBoxesStatus;
        return this;
    }

    public Boolean getForceMaximized() {
        return forceMaximized;
    }

    public WindowParams setForceMaximized(Boolean forceMaximized) {
        this.forceMaximized = forceMaximized;
        return this;
    }

    public Boolean getLazySetupInMaximizedMode() {
        return lazySetupInMaximizedMode;
    }

    public WindowParams setLazySetupInMaximizedMode(Boolean lazySetupInMaximizedMode) {
        this.lazySetupInMaximizedMode = lazySetupInMaximizedMode;
        return this;
    }

    public Integer getMaxCachedAppsInMaximizedMode() {
        return maxCachedAppsInMaximizedMode;
    }

    public WindowParams setMaxCachedAppsInMaximizedMode(Integer maxCachedAppsInMaximizedMode) {
        this.maxCachedAppsInMaximizedMode = maxCachedAppsInMaximizedMode;
        return this;
    }

    public WindowOriginSize getOriginSize() { return originSize; }

    public WindowParams setOriginSize(WindowOriginSize originSize) {
        this.originSize = originSize;
        return this;
    }

    public PageScaleRange getPageScaleRange() { return pageScaleRange; }

    public WindowParams setPageScaleRange(PageScaleRange pageScaleRange) {
        this.pageScaleRange = pageScaleRange;
        return this;
    }
}
