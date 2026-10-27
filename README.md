# RitualFocus

本地优先的习惯打卡 + 番茄专注 Android 应用。数据只存在本机，无账号、无网络依赖。

当前版本：**0.2.0**

## 功能

- **习惯打卡**：32 个 emoji 图标与 12 种配色可选；支持每天 / 每周固定星期 / 间隔 N 天三种打卡计划，休息日不计入完成率；连续天数统计；7 天彩带庆祝、30 天成就徽章；点击习惯可删除或修改计划。
- **专注计时**：专注 / 休息自动循环，环形进度显示；采用时间戳对齐，退到后台或重启应用后按真实时间结算；周期结束发送系统通知。
- **统计**：今日专注、今日打卡、最长连续、本月完成率四张统计卡；习惯月 / 周热力图；年度专注热力图，点按格子查看当天明细。
- **设置**：专注与休息时长（10–90 分钟，未开始计时时立即生效）；跟随系统 / 浅色 / 深色主题；JSON、CSV 数据导出。

## 预览

`preview/index.html` 是合并两个原始 HTML 后的单文件预览版，无需构建，浏览器直接打开即可体验三标签（习惯 / 专注 / 统计）完整交互。

## 技术栈

| 项目 | 版本 |
| --- | --- |
| 语言 | Kotlin 2.1.20 |
| UI | Jetpack Compose（BOM 2025.06.01）+ Material3 |
| 构建 | AGP 8.13.0、Gradle 8.14.3、JDK 17 |
| SDK | minSdk 26、compileSdk / targetSdk 36 |
| 包名 | `com.ritualfocus.app` |

数据通过 SharedPreferences + `org.json` 持久化，时间处理使用 `java.time`。

## 目录结构

```
android/                    原生 Android 工程
  app/src/main/java/com/ritualfocus/app/
    data/                   数据模型与持久化
    ui/                     ViewModel、根组件、主题
    ui/screens/             习惯 / 专注 / 统计 / 添加 / 设置
    ui/components/          玻璃卡片、热力图、环形进度、动效
    util/                   日期、统计、导出、通知
preview/index.html          单文件 HTML 预览
```

## 构建

环境要求：JDK 17、Android SDK Platform 36。

方式一：用 Android Studio 打开 `android/` 目录，等待 Gradle 同步后运行 `app`。

方式二：命令行使用已安装的 Gradle 8.14.3：

```bash
gradle -p android :app:assembleDebug
```

产物位于 `android/app/build/outputs/apk/debug/app-debug.apk`。

> 仓库未包含 Gradle Wrapper 与 `local.properties`；请按本机 SDK 路径自行生成，构建产物与本地配置均已被 `.gitignore` 忽略。
