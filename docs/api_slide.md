# Slide API

## slideOpenUrl

### 代码示例

用户可以通过 `slideOpenUrl` 方法来监听幻灯片中的链接点击事件。当用户点击幻灯片中的链接时，将触发该回调函数。

```shell
whiteSdk.setSlideListener(new SlideListener() {
    @Override
    public void slideOpenUrl(String url) {
        runOnUiThread(() -> showToast("Open URL: " + url));
    }
});
```

## 设置自定义链接 customLinks

通过 customLinks 参数，用户可以为特定页码和 slideId 的幻灯片添加自定义点击链接。当用户点击幻灯片中设置的链接区域时，将触发对应的回调函数。

适用场景

* PPT 幻灯片中包含超链接需求，如跳转网页、打开外部资源等。
* 为特定幻灯片设置跳转地址以增强交互性。

### 如何获取 slideId？

参考 [已转换PPT添加自定义link](https://github.com/netless-io/netless-slide-demo?tab=readme-ov-file#%E5%B7%B2%E8%BD%AC%E6%8D%A2ppt%E6%B7%BB%E5%8A%A0%E8%87%AA%E5%AE%9A%E4%B9%89link)
文档。

### 代码示例

```shell
// 监听回调, 自定义链接点击事件
whiteSdk.setSlideListener(new SlideListener() {
    @Override
    public void slideOpenUrl(String url) {
        runOnUiThread(() -> showToast("Open URL: " + url));
    }
});

// 设置自定义链接
String prefixUrl = "https://convertcdn.netless.link/dynamicConvert";
String taskUuid = "47f359400ab1444986872db1723bb793";
WhiteSlideCustomLink[] customLinks = new WhiteSlideCustomLink[]{
        new WhiteSlideCustomLink(1, "slide-9", "https://www.shengwang.cn?t=1"),
        new WhiteSlideCustomLink(1, "slide-2", "https://www.shengwang.cn?t=2"),
};
WindowAppParam param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, "Projector App", customLinks);
mRoom.addApp(param, insertPromise);
```