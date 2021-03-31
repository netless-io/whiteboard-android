package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * 输出日志选项。
 */
public class LoggerOptions extends WhiteObject {

    /**
     * 日志等级。
     * <p>
     * 日志级别顺序依次为 `error`、`warn`、`info`、和 `debug`。选择一个级别，你就可以看到在该级别之前所有级别的日志信息。
     * 例如，你选择 `info` 级别，就可以看到在 `error`、`warn`、`info` 级别上的所有日志信息。
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
         * （默认）总是上报。
         */
        @SerializedName("alwaysReport")
        always,
        /**
         * 禁止上报。
         */
        @SerializedName("banReport")
        ban,
    }

    private Boolean disableReportLog;

    public Boolean getDisableReportLog() {
        return disableReportLog;
    }

    /**
     * 开启/关闭日志上报。
     *
     * @param disableReportLog 是否关闭日志上报；
     *                         - `true`：关闭。
     *                         - `false`：（默认）开启错误日志上报。
     * @deprecated 该方法已废弃。请使用 {@link #getReportDebugLogMode()}、{@link #getReportQualityMode()} 和 {@link #getReportLevelMask()}。
     */
    public void setDisableReportLog(Boolean disableReportLog) {
        if (disableReportLog) {
            setReportDebugLogMode(ReportMode.ban);
            setReportQualityMode(ReportMode.ban);
        }
    }

    /**
     * 获取日志打印等级。
     *
     * @return 日志打印等级。详见 {@link Level Level}。
     */
    public Level getPrintLevelMask() {
        return printLevelMask;
    }

    /**
     * 设置日志打印等级。
     *
     * @param printLevelMask 日志打印等级，详见 {@link Level Level}。默认等级为 `info`。
     * @since 2.11.10
     */
    public void setPrintLevelMask(Level printLevelMask) {
        this.printLevelMask = printLevelMask;
    }

    /**
     * 获取 SDK 的日志上报等级。
     *
     * @return 日志上报等级，详见 {@link Level Level}。
     */
    public Level getReportLevelMask() {
        return reportLevelMask;
    }

    /**
     * 设置 SDK 上报的日志等级。
     *
     * @param reportLevelMask 日志上报等级，详见 {@link Level Level}。默认等级为 `info`。
     * @since 2.11.10
     */
    public void setReportLevelMask(Level reportLevelMask) {
        this.reportLevelMask = reportLevelMask;
    }

    /**
     * 获取 SDK 上报日志信息的模式。
     *
     * @return 上报模式，详见 {@link ReportMode ReportMode}。
     */
    public ReportMode getReportDebugLogMode() {
        return reportDebugLogMode;
    }

    /**
     * 设置 SDK 上报 `debug` 等级日志的模式。
     *
     * @param reportDebugLogMode 上报模式，详见 {@link ReportMode ReportMode}。默认为总是上报。
     * @since 2.11.10
     */
    public void setReportDebugLogMode(ReportMode reportDebugLogMode) {
        this.reportDebugLogMode = reportDebugLogMode;
    }

    /**
     * 获取 SDK 上报连接质量数据的模式。
     *
     * @return SDK 上报连接质量数据的模式。
     */
    public ReportMode getReportQualityMode() {
        return reportQualityMode;
    }

    /**
     * 设置 SDK 上报连接质量数据的模式。
     * <p>
     * 连接质量数据包括连接时长和连接稳定性等。
     *
     * @param reportQualityMode 上报模式，详见 {@link ReportMode ReportMode}。默认为总是上报。
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
