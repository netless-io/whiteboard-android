package com.herewhite.sdk.domain;

public class LoggerOptions extends WhiteObject {
    private Boolean disableReportLog;

    public Boolean getDisableReportLog() {
        return disableReportLog;
    }

    /**
     * 日志上报系统开关
     *
     * @param disableReportLog 是否关闭日志上报。默认 false，即上报错误日志
     */
    public void setDisableReportLog(Boolean disableReportLog) {
        this.disableReportLog = disableReportLog;
    }
}
