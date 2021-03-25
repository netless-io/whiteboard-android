package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

public class LoggerOptions extends WhiteObject {

    /**
     * 最底层的 level，会包含上层日志信息
     *
     * @since 2.11.10
     */
    public enum Level {
        /**
         * 调试日志：最详细的日志，目前内容与 info 一致
         */
        debug,
        /**
         * 信息日志：主要为连接状态
         */
        info,
        /**
         * 警告日志：主要为对开发者传入的部分不符合 sdk 参数时，进行自动调整的警告（API 弃用警告不会在上报）
         */
        warn,
        /**
         * 报错日志：直接导致 sdk 无法正常运行的信息
         */
        error,
    }

    /**
     * @since 2.11.10
     */
    public enum ReportMode {
        /**
         * 总是上报
         */
        @SerializedName("alwaysReport")
        always,
        /**
         * 不上报
         */
        @SerializedName("banReport")
        ban,
    }

    private Boolean disableReportLog;

    public Boolean getDisableReportLog() {
        return disableReportLog;
    }

    /**
     * 日志上报系统开关
     *
     * @param disableReportLog 是否关闭日志上报。默认 false，即上报错误日志
     * @deprecated 已弃用，请使用 {@link #getReportDebugLogMode()} {@link #getReportQualityMode()} 以及 {@link #getReportLevelMask()}
     */
    public void setDisableReportLog(Boolean disableReportLog) {
        if (disableReportLog) {
            setReportDebugLogMode(ReportMode.ban);
            setReportQualityMode(ReportMode.ban);
        }
    }

    public Level getPrintLevelMask() {
        return printLevelMask;
    }

    /**
     * webview 中，日志打印等级（默认 info）
     *
     * @since 2.11.10
     */
    public void setPrintLevelMask(Level printLevelMask) {
        this.printLevelMask = printLevelMask;
    }

    public Level getReportLevelMask() {
        return reportLevelMask;
    }

    /**
     * sdk 日志信息上报等级（默认 info）
     *
     * @since 2.11.10
     */
    public void setReportLevelMask(Level reportLevelMask) {
        this.reportLevelMask = reportLevelMask;
    }

    public ReportMode getReportDebugLogMode() {
        return reportDebugLogMode;
    }

    /**
     * sdk 日志信息上报
     * 默认上报
     *
     * @since 2.11.10
     */
    public void setReportDebugLogMode(ReportMode reportDebugLogMode) {
        this.reportDebugLogMode = reportDebugLogMode;
    }

    public ReportMode getReportQualityMode() {
        return reportQualityMode;
    }

    /**
     * 质量连接日志上报配置
     * 默认上报
     *
     * @since 2.11.10
     */
    public void setReportQualityMode(ReportMode reportQualityMode) {
        this.reportQualityMode = reportQualityMode;
    }

    private Level printLevelMask;
    private Level reportLevelMask;
    private ReportMode reportDebugLogMode;
    private ReportMode reportQualityMode;
}
