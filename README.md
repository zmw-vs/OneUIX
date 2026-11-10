# BatteryInfoLSP

一个**无界面、常开**的 LSPosed 模块，从 [SoClear/OneUIX](https://github.com/SoClear/OneUIX)
里提取的两个小功能，打成一个 APK：

1. **显示更多电池信息** — 「设置 → 关于手机 → 电池信息」显示被隐藏的电池监管信息项。
2. **解除 FCM 网络限制** — 去掉国行/港版固件对 GMS/FCM 推送的网络限制。

## 来源与许可

- 派生自 [SoClear/OneUIX](https://github.com/SoClear/OneUIX)，快照 commit `93b2dbd` 中的
  `showMoreBatteryInfo` 与 `liftFcmNetworkLimit` 两个功能。
- 原作者：SoClear。原项目采用 **GNU AGPL-3.0**，本模块同样以 **AGPL-3.0** 发布，
  许可证全文见 `LICENSE.txt`。任何分发（包括二进制 APK）都必须附上源码与许可证，
  并保留上述来源说明。

## 实现原理

| 功能 | 进程 | Hook 点 |
|---|---|---|
| 显示更多电池信息 | `com.android.settings` | `BatteryRegulatoryPreferenceController.getAvailabilityStatus()` 恒返回 `0`（AVAILABLE），时机为宿主 `Application.attach` 之后 |
| 解除 FCM 网络限制 | `system`（system_server） | Hook `com.android.server.alarm.GmsAlarmManager` 的全部构造器，构造完成后把 `isChinaMode` / `isHongKongMode` 两个字段置 `false` |

上游 FCM 那条依赖一个 429 行的通用反射门面 `Reflect.kt`，这里用 12 行原生反射
（沿父类链找字段 + `setBoolean`）替代，行为等价。

## 相对上游砍掉了什么

配置通道（`preference.json` / DataStore / libxposed service）、整个 Compose 设置界面、
DexKit、NDK + CMake、`:stub` 模块、另外 40 多个 Hook、30 个作用域。
最终只保留：1 个 Kotlin 入口文件 + 3 个 xposed 元数据 + 1 个 Manifest + 2 条字符串。

## 工程结构

```
app/src/main/java/com/zmw/batteryinfo/BatteryInfoModule.kt   Hook 入口（两个回调）
app/src/main/AndroidManifest.xml                             无 Activity 的 application 壳
app/src/main/res/values/strings.xml                          模块名与描述
app/src/main/resources/META-INF/xposed/java_init.list        入口类名
app/src/main/resources/META-INF/xposed/module.prop           API 102 声明
app/src/main/resources/META-INF/xposed/scope.list             system + com.android.settings
.github/workflows/build.yml                                  一键出 APK
```

## 构建

本机需 JDK 25 + Android SDK（compileSdk 37 / minSdk 33）：

```
gradlew assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`。

本机没有这套工具链时，把这个目录推到任意 GitHub 仓库即可由
`.github/workflows/build.yml` 自动构建并在 Actions 产物里下载 APK。

> 工程**未开启混淆**，以保证 `java_init.list` 中的入口类名始终有效。
> 若日后开启 R8/ProGuard，必须追加规则
> `-adaptresourcefilecontents META-INF/xposed/java_init.list` 并 keep XposedModule 子类。

## 安装使用

1. 设备需 Root + 已激活的 LSPosed（支持 libxposed API 102）。
2. 安装 APK（无桌面图标，属正常现象）。
3. 在 LSPosed 管理器启用本模块，作用域勾选 **「系统框架 / system」** 和
   **「系统设置 / com.android.settings」**。
4. **重启手机**（FCM 那条跑在 system_server，必须重启；电池那条重启设置 App 即可生效）。

## 已知限制

- 需要 Samsung One UI 5.0+ / Android 13+；类名硬编码，One UI 升级后可能改名，
  此时 Hook 会优雅失败并只写日志（见 LSPosed 日志，tag 前缀 `[BatteryInfoLSP]`）。
- 常开无开关；如需临时关闭，在 LSPosed 里停用模块即可。
- 不支持传统 `XposedHelpers` 的老版框架，仅适配 libxposed API 102。
