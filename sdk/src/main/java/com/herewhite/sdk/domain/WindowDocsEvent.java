package com.herewhite.sdk.domain;

public class WindowDocsEvent {
    public static WindowDocsEvent PrevPage = new WindowDocsEvent("prevPage");
    public static WindowDocsEvent NextPage = new WindowDocsEvent("nextPage");
    public static WindowDocsEvent PrevStep = new WindowDocsEvent("prevStep");
    public static WindowDocsEvent NextStep = new WindowDocsEvent("nextStep");

    // 文档事件
    private String event;

    // 事件参数
    private Options options = new Options();

    public WindowDocsEvent(String event) {
        this.event = event;
    }

    /**
     * @param event 文档事件。包括以下几种：
     * prevPage：上一页。
     * nextPage: 下一页。
     * prevStep：上一步。
     * nextStep：下一步。
     * jumpToPage：跳转至页码。
     * scalePage：缩放当前课件页，scale 取值范围为 1 到 4，可传入小数。
     *
     * @param options 事件参数。event 为 "jumpToPage" 时传入 page；event 为 "scalePage" 时传入 scale。
     */
    public WindowDocsEvent(String event, Options options) {
        this.event = event;
        this.options = options;
    }

    public static WindowDocsEvent JumpToPage(Integer page) {
        Options options = new Options();
        options.page = page;
        return new WindowDocsEvent("jumpToPage", options);
    }

    public static WindowDocsEvent ScalePage(Double scale) {
        Options options = new Options();
        options.scale = scale;
        return new WindowDocsEvent("scalePage", options);
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public static class Options extends WhiteObject {
        private Integer page;
        private Double scale;

        public Options() {}

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Double getScale() {
            return scale;
        }

        public void setScale(Double scale) {
            this.scale = scale;
        }
    }
}
