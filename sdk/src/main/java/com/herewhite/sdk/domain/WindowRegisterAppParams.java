package com.herewhite.sdk.domain;

import java.util.Map;

public class WindowRegisterAppParams extends WhiteObject {
    // 用本地 js 代码注册
    private String javascriptString;
    // 注册的 app 名称
    private String kind;
    // 用发布包代码注册
    private String url;
    // 初始化 app 实例时，会被传入的参数。这段配置不会被同步其他端，属于本地设置。常常用来设置 debug 的开关。
    private Map<String, Object> appOptions;
    // 挂载在 window 上的变量名，挂在后为 window.variable
    private String variable;

    /**
     * 以本地脚本方式，构建注册参数
     *
     * @param javascriptString
     * @param kind  App 的类型
     * @param variable 注册后挂在到window的名称
     * @param appOptions app 参数
     */
    public WindowRegisterAppParams(String javascriptString, String kind, String variable, Map<String, Object> appOptions) {
        this.javascriptString = javascriptString;
        this.kind = kind;
        this.appOptions = appOptions;
        this.variable = variable;
    }

    public WindowRegisterAppParams(String url, String kind, Map<String, Object> appOptions) {
        this.url = url;
        this.kind = kind;
        this.appOptions = appOptions;
    }
}
