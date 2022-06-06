package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class WindowAppParam {
    public static final String KIND_DOCSVIEWER = "DocsViewer";
    public static final String KIND_MEDIAPLAYER = "MediaPlayer";
    // for kind of new ppt
    public static final String KIND_SLIDE = "Slide";

    private String kind;
    private Options options;
    private Attributes attributes;

    public WindowAppParam(String kind, Options options, Attributes attributes) {
        this.kind = kind;
        this.options = options;
        this.attributes = attributes;
    }

    public String getKind() {
        return kind;
    }

    public Options getOptions() {
        return options;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public static WindowAppParam createDocsViewerApp(String scenePath, Scene[] scenes, String title) {
        DocOptions options = new DocOptions(scenePath, scenes, title);
        return new WindowAppParam(KIND_DOCSVIEWER, options, null);
    }

    public static WindowAppParam createMediaPlayerApp(String src, String title) {
        PlayerOptions options = new PlayerOptions(title);
        PlayerAttributes attributes = new PlayerAttributes(src);
        return new WindowAppParam(KIND_MEDIAPLAYER, options, attributes);
    }

    public static WindowAppParam createSlideApp(String scenePath, Scene[] scenes, String title) {
        SlideOptions options = new SlideOptions(scenePath, scenes, title);
        return new WindowAppParam(KIND_SLIDE, options, null);
    }

    /**
     * 构建由新转换服务转换的 App 参数
     * @param taskUuid
     * @param prefixUrl
     * @param title
     * @return
     */
    public static WindowAppParam createSlideApp(String taskUuid, String prefixUrl, String title) {
        if (!prefixUrl.startsWith("http")) {
            throw new IllegalArgumentException("params error, check taskUuid and prefixUrl");
        }
        return new WindowAppParam(
                KIND_SLIDE,
                new ProjectorOptions(String.format("/%s/%s", taskUuid, UUID.randomUUID()), title),
                new ProjectorAttributes(taskUuid, prefixUrl)
        );
    }

    private static class DocOptions extends Options {
        private final String scenePath;
        private final Scene[] scenes;

        public DocOptions(String scenePath, Scene[] scenes, String title) {
            super(title);
            this.scenePath = scenePath;
            this.scenes = scenes;
        }
    }

    private static class SlideOptions extends Options {
        private final String scenePath;
        private final Scene[] scenes;

        public SlideOptions(String scenePath, Scene[] scenes, String title) {
            super(title);
            this.scenePath = scenePath;
            this.scenes = scenes;
        }

        public SlideOptions(String scenePath, String title) {
            super(title);
            this.scenePath = scenePath;
            this.scenes = null;
        }
    }

    private static class PlayerOptions extends Options {
        public PlayerOptions(String title) {
            super(title);
        }
    }

    private static class PlayerAttributes extends Attributes {
        private final String src;

        public PlayerAttributes(String src) {
            this.src = src;
        }
    }

    private static class ProjectorOptions extends Options {
        private final String scenePath;

        public ProjectorOptions(String scenePath, String title) {
            super(title);
            this.scenePath = scenePath;
        }
    }

    private static class ProjectorAttributes extends Attributes {
        @SerializedName("taskId")
        private final String taskUuid;
        @SerializedName("url")
        private final String prefixUrl;

        public ProjectorAttributes(String taskUuid, String prefixUrl) {
            this.taskUuid = taskUuid;
            this.prefixUrl = prefixUrl;
        }
    }


    public static class Options extends WhiteObject {
        private String title;

        public Options(String title) {
            this.title = title;
        }
    }

    public static class Attributes extends WhiteObject {

    }
}
