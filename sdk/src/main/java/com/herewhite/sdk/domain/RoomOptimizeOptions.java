package com.herewhite.sdk.domain;


/**
 * 白板渲染优化配置项。
 */
public class RoomOptimizeOptions extends WhiteObject {

    /**
     * 表示动画绘制频率，毫秒值 0～120, 建议 20 的倍数. 数值越大，动画绘制频率越低，性能越好，但是动画效果越差。
     * 性能比较差的设备/大屏设备，建议设置成比较大的值，比如 60
     */
    private Integer useLowTaskAnimation;

    /**
     * 是否使用单个画布，用于减少dom重绘次数的
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
