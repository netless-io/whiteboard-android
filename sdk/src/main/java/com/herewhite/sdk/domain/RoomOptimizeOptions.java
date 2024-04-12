package com.herewhite.sdk.domain;


/**
 * 白板渲染优化配置项。
 */
public class RoomOptimizeOptions extends WhiteObject {
    /**
     * 白板绘制的刷新间隔：刷新间隔越低，笔迹显示越流畅，性能开销越大；刷新间隔越高，笔迹越卡，性能消耗越少
     * 间隔为毫秒值 0～120, 建议设置 20 的倍数。性能比较差的设备/大屏设备，建议设置成比较大的值，比如 60
     */
    private Integer useLowTaskAnimation;

    /**
     * 是否使用单个画布
     * 白板默认使用两个画布交替绘制以避免在部分设备上绘制时可能出现的画面闪烁现象，使用单个画布绘制可以避免重绘，降低性能消耗
     */
    private Boolean useSinglerCanvas;

    public Integer getUseLowTaskAnimation() {
        return useLowTaskAnimation;
    }

    public void setUseLowTaskAnimation(Integer useLowTaskAnimation) {
        this.useLowTaskAnimation = useLowTaskAnimation;
    }

    public Boolean getUseSinglerCanvas() {
        return useSinglerCanvas;
    }

    public void setUseSinglerCanvas(Boolean useSinglerCanvas) {
        this.useSinglerCanvas = useSinglerCanvas;
    }
}
