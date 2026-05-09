# SDK Instrumentation Tests

`sdk` 模块的真实联调测试支持 3 种配置来源，优先级从高到低如下：

1. instrumentation runner args
2. 仓库内的 `SdkTestDefaults.DEFAULT_*` fallback 常量

这样在 Android Studio 里对 `sdk/src/androidTest` 里的 Test 右键运行时，也能直接拿到本地配置，不需要把值编进 `BuildConfig`，也不需要在设备端读额外文件。

## Recommended local fallback

fallback 文件是：

- `sdk/src/androidTest/java/com/herewhite/sdk/local/SdkTestDefaults.java`

仓库里默认提交的是空值版本；本地如果希望右键直接运行，可以执行同步脚本覆盖它。

为避免误提交本地凭据，建议安装仓库内的 git hook：

```bash
bash ./scripts/install-git-hooks.sh
```

提交前会自动执行：

```bash
bash ./scripts/check-sdk-test-defaults.sh
```

如果 `SdkTestDefaults.java` 中仍然包含非空本地值，提交会被拦截。

## Sync from example config

如果你已经在 `app/src/main/res/values/string_white_sdk_config.xml` 里填好了示例配置，可以直接同步：

```bash
./scripts/sync-sdk-test-config.sh
```

脚本会把 `sdk_app_id`、`room_uuid`、`room_token` 同步到 `SdkTestDefaults.java`。

## Preferred command line usage

命令行或 CI 推荐显式传 instrumentation args：

```bash
./gradlew :sdk:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.whiteboard.test.appIdentifier=your-app-identifier \
  -Pandroid.testInstrumentationRunnerArguments.whiteboard.test.roomUuid=your-room-uuid \
  -Pandroid.testInstrumentationRunnerArguments.whiteboard.test.roomToken=your-room-token
```

## Optional environment variables

- `WHITEBOARD_TEST_APP_IDENTIFIER`
- `WHITEBOARD_TEST_ROOM_UUID`
- `WHITEBOARD_TEST_ROOM_TOKEN`

## Behavior without credentials

如果 runner args 和 `SdkTestDefaults` 都没有提供完整配置，`sdk` 的集成测试会通过 `Assume` 跳过。
