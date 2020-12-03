package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

public class FontFace extends WhiteObject {

    public FontFace(String name, String src) {
        this.fontFamily = name;
        this.src = src;
    }

    @SerializedName("font-family")
    private String fontFamily;
    private String src;

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public String getUnicodeRange() {
        return unicodeRange;
    }

    public void setUnicodeRange(String unicodeRange) {
        this.unicodeRange = unicodeRange;
    }

    @SerializedName("font-style")
    private String fontStyle;
    @SerializedName("font-weight")
    private String fontWeight;
    @SerializedName("unicode-range")
    private String unicodeRange;
}
