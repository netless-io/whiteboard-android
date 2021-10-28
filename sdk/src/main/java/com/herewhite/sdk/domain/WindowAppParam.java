package com.herewhite.sdk.domain;

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

    private static class DocOptions extends Options {
        private final String scenePath;
        private final Scene[] scenes;
        private final String title;

        public DocOptions(String scenePath, Scene[] scenes, String title) {
            this.scenePath = scenePath;
            this.scenes = scenes;
            this.title = title;
        }
    }

    private static class SlideOptions extends Options {
        private final String scenePath;
        private final Scene[] scenes;
        private final String title;

        public SlideOptions(String scenePath, Scene[] scenes, String title) {
            this.scenePath = scenePath;
            this.scenes = scenes;
            this.title = title;
        }
    }

    private static class PlayerOptions extends Options {
        private final String title;

        public PlayerOptions(String title) {
            this.title = title;
        }
    }

    private static class PlayerAttributes extends Attributes {
        private final String src;

        public PlayerAttributes(String src) {
            this.src = src;
        }
    }

    public static class Options extends WhiteObject {

    }

    public static class Attributes extends WhiteObject {

    }
}
