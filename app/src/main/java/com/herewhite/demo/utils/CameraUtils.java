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
}
