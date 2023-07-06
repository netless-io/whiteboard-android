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

    public WindowDocsEvent(String event, Options options) {
        this.event = event;
        this.options = options;
    }

    public static WindowDocsEvent JumpToPage(Integer page) {
        Options options = new Options();
        options.page = page;
        return new WindowDocsEvent("jumpToPage", options);
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

        public Options() {}

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }
    }
}
