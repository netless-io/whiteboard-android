package com.herewhite.sdk.domain;

/**
 * 场景类。
 */
public class Scene extends WhiteObject {

    private String name;
    private Long componentsCount;
    private PptPage ppt;

    /**
     * `Scene` 构造方法，用于初始化场景实例。
     *
     * @note 在插入调用该方法初始化的场景时，SDK 会随机给新场景命名。
     */
    public Scene() {
    }

    /**
     * `Scene` 构造方法，用于初始化场景实例。
     *
     * @param name 场景名称。
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * `Scene` 构造方法，用于初始化场景实例。
     *
     * @param name 场景名称。
     * @param ppt  场景背景图实例配置。详见 {@link PptPage PptPage}。
     */
    public Scene(String name, PptPage ppt) {
        this.name = name;
        this.ppt = ppt;
    }

    /**
     * 获取场景名称。
     *
     * @return 场景名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 设置场景名称。
     *
     * @param name 场景名称。
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取场景中的笔画数。
     *
     * @return 场景中的笔画数。
     */
    public Long getComponentsCount() {
        return componentsCount;
    }

    /**
     * 获取场景背景图的配置信息。
     *
     * @return 场景背景图的配置信息，详见 {@link PptPage PptPage}。
     */
    public PptPage getPpt() {
        return ppt;
    }

    /**
     * 设置场景背景图。
     *
     * @param ppt 场景背景图实例的配置信息，详见 {@link PptPage PptPage}。
     */
    public void setPpt(PptPage ppt) {
        this.ppt = ppt;
    }
}
