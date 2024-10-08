package com.herewhite.sdk.domain;

public enum SlideErrorType {
    /**
     * ResourceError: 当 PPT 依赖的远程资源（如 JSON、PNG）不可用时触发，
     * 触发后当前页无法交互。
     * 恢复手段：重新渲染当前页或者跳转到下一页。
     */
    RESOURCE_ERROR,

    /**
     * RuntimeError: 未知的异常导致触发，触发后当前页无法交互。
     * 恢复手段：跳转到下一页。
     */
    RUNTIME_ERROR,

    /**
     * RuntimeWarn: 动画过程中出现未知的警告，触发后动画当前帧表现异常，
     * 但不影响下一帧和页面交互。
     * 恢复手段：无需特殊处理。
     */
    RUNTIME_WARN,

    /**
     * CanvasCrash: 由于内存不足，或者 canvas 被意外移除（在未调用 slide.destroy() 的情况下移除 canvas 元素）
     * 导致触发，触发后 canvas 元素白屏。
     * 恢复手段：刷新网页，或者销毁 slide 对象并重新创建。
     */
    CANVAS_CRASH,

    /**
     * 未知错误类型
     */
    UNKNOWN
}
