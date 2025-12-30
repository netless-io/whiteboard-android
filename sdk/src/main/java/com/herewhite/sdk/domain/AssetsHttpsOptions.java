package com.herewhite.sdk.domain;

import androidx.annotation.NonNull;

/**
 * Internal options for configuring Assets HTTPS loading.
 * <p>
 * Default values are aligned with the stable SDK behavior.
 * This class is intended for internal or controlled customization only.
 */
public final class AssetsHttpsOptions {
    // https://sdk.herewhite.com/assets/whiteboard/index.html
    private static final String DEFAULT_PROTOCOL = "https";
    private static final String DEFAULT_DOMAIN = "sdk.herewhite.com";
    private static final String DEFAULT_ASSETS_PATH = "/assets/";
    private static final String DEFAULT_ENTRY_PATH = "whiteboard/index.html";

    private String protocol = DEFAULT_PROTOCOL;
    private String domain = DEFAULT_DOMAIN;
    private String assetsPath = DEFAULT_ASSETS_PATH;
    private String entryPath = DEFAULT_ENTRY_PATH;

    @NonNull
    public String getDomain() {
        return domain;
    }

    public AssetsHttpsOptions setDomain(@NonNull String domain) {
        this.domain = trimSlashes(domain);
        return this;
    }

    public AssetsHttpsOptions setProtocol(@NonNull String protocol) {
        this.protocol = protocol.replace("://", "");
        return this;
    }

    @NonNull
    public String getAssetsPath() {
        return assetsPath;
    }

    @NonNull
    public String getEntryPath() {
        return entryPath;
    }

    @NonNull
    public String buildEntryUrl() {
        return protocol + "://" + domain + assetsPath + trimLeadingSlash(entryPath);
    }

    public AssetsHttpsOptions setAssetsPath(@NonNull String assetsPath) {
        this.assetsPath = normalizePath(assetsPath);
        return this;
    }

    public AssetsHttpsOptions setEntryPath(@NonNull String entryPath) {
        this.entryPath = trimLeadingSlash(entryPath);
        return this;
    }

    private static String normalizePath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return path;
    }

    private static String trimLeadingSlash(String value) {
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value;
    }

    private static String trimSlashes(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '/') start++;
        while (end > start && value.charAt(end - 1) == '/') end--;
        return value.substring(start, end);
    }
}