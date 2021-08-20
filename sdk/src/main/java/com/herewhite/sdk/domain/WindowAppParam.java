package com.herewhite.sdk.domain;

public class WindowAppParam {
    public static final String KIND_DOCSVIEWER = "DocsViewer";
    public static final String KIND_MEDIAPLAYER = "MediaPlayer";

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

    public static WindowAppParam createDocsViewerApp(String dir, Scene[] scenes, String title) {
        DocOptions options = new DocOptions(dir, scenes, title);
        return new WindowAppParam(KIND_DOCSVIEWER, options, null);
    }

    public static WindowAppParam createMediaPlayerApp(String src, String title) {
        PlayerOptions options = new PlayerOptions(title);
        PlayerAttributes attributes = new PlayerAttributes(src);
        return new WindowAppParam(KIND_MEDIAPLAYER, options, attributes);
    }

    private static class DocOptions extends Options {
        private final String dir;
        private final Scene[] scenes;
        private final String title;

        public DocOptions(String dir, Scene[] scenes, String title) {
            this.dir = dir;
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
