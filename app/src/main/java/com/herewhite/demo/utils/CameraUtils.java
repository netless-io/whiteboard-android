package com.herewhite.demo.utils;

import android.content.Context;

import com.herewhite.sdk.WhiteboardView;

public class CameraUtils {

    public static void offsetXY(WhiteboardView whiteboardView, double imageWidth, double imageHeight) {
        double whiteboardWidth = px2dp(whiteboardView.getContext(), whiteboardView.getWidth());
        double whiteboardHeight = px2dp(whiteboardView.getContext(), whiteboardView.getHeight());

        double scaleByW = whiteboardWidth / imageWidth;
        double scaleByH = whiteboardHeight / imageHeight;
        double scale;
        double offsetX = 0;
        double offsetY = 0;
        if (scaleByH < scaleByW) {
            scale = scaleByW;
            double viewportHeight = whiteboardHeight / scale;
            offsetY = (viewportHeight - imageHeight) / 2;
        } else {
            scale = scaleByH;
            double viewportWidth = whiteboardWidth / scale;
            offsetX = (viewportWidth - imageWidth) / 2;
        }
    }

    public static float px2dp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }


public static float getScale(Context context, WhiteboardView whiteboardView) {
    // 假定 insertImage 图片信息
    int imageWidth = 600;
    int imageHeight = 400;

    // View 容器的宽高 DP 值，即白板坐标系 scale 为 1 可是宽高
    int whiteboardWidth = (int) px2dp(context, whiteboardView.getWidth());
    int whiteboardHeight = (int) px2dp(context, whiteboardView.getHeight());

    float scaleByW = whiteboardWidth * 1f / imageWidth;
    float scaleByH = whiteboardHeight * 1f / imageHeight;

    // Center 模式的 scale 值
    float scale = Math.min(scaleByW, scaleByH);
    // CenterCrop 模式的 scale 值
    // float scale = Math.max(scaleByW, scaleByH);

    return scale;
}
}
