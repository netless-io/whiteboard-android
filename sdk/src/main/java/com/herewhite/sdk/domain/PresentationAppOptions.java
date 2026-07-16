package com.herewhite.sdk.domain;

public class PresentationAppOptions extends WhiteObject {
    private Boolean disableCameraTransform;
    private Double maxCameraScale;
    private PresentationViewport viewport;
    private Boolean justDocsViewReadonly;
    private Boolean useScrollbar;
    private Boolean debounceSync;
    private Boolean goToPageByClick;
    private Boolean useClipView;

    public Boolean getDisableCameraTransform() { return disableCameraTransform; }
    public PresentationAppOptions setDisableCameraTransform(Boolean value) { disableCameraTransform = value; return this; }
    public Double getMaxCameraScale() { return maxCameraScale; }
    public PresentationAppOptions setMaxCameraScale(Double value) { maxCameraScale = value; return this; }
    public PresentationViewport getViewport() { return viewport; }
    public PresentationAppOptions setViewport(PresentationViewport value) { viewport = value; return this; }
    public Boolean getJustDocsViewReadonly() { return justDocsViewReadonly; }
    public PresentationAppOptions setJustDocsViewReadonly(Boolean value) { justDocsViewReadonly = value; return this; }
    public Boolean getUseScrollbar() { return useScrollbar; }
    public PresentationAppOptions setUseScrollbar(Boolean value) { useScrollbar = value; return this; }
    public Boolean getDebounceSync() { return debounceSync; }
    public PresentationAppOptions setDebounceSync(Boolean value) { debounceSync = value; return this; }
    public Boolean getGoToPageByClick() { return goToPageByClick; }
    public PresentationAppOptions setGoToPageByClick(Boolean value) { goToPageByClick = value; return this; }
    public Boolean getUseClipView() { return useClipView; }
    public PresentationAppOptions setUseClipView(Boolean value) { useClipView = value; return this; }
}
