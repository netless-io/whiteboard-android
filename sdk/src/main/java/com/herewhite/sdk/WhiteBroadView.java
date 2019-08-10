package com.herewhite.sdk;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 修复类名，此类为向前兼容类
 * @deprecated 请使用 WhiteboardView 类
 */
public class WhiteBroadView extends WhiteboardView {

    public WhiteBroadView(Context context) {
        super(context);
    }

    public WhiteBroadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
