# TempShot · 一刻相册

一个隐私优先的原生 Android 临时相册工具：照片仅在本机处理，不联网、不上传；可为新加入的照片设定保留期限，并通过系统回收站处理到期照片。

## 下载与安装

发布版 APK：[`releases/TempShot-0.2.5-release.apk`](releases/TempShot-0.2.5-release.apk)

- 最低支持 Android 12（API 31）。
- 若手机已安装同包名的 debug 版，请先卸载 debug 版，再安装此 release 版；之后使用同一签名构建的 release 版本可直接覆盖更新。

## 功能

- 保留期限：15 分钟、12 小时、1 天、3 天或 1 周；调整仅影响后续新纳入的照片。
- 自动纳入相机照片、截图或其他 App 保存的图片；首次扫描的历史图片、收藏图片及未知目录不会自动纳入。
- 手动管理：到期后发送通知，由用户确认后移入系统回收站。
- 自动管理：在已授予完整照片访问和媒体管理权限时，于后台检查中尝试将到期图片移入系统回收站。
- 中英文界面；实际文件名与目录名保持原文。
- 到期图片进入系统回收站，可由系统或用户恢复。

## 隐私与限制

- APP 不请求网络权限，不上传或同步照片。
- 后台检查最短周期为 15 分钟，并可能受 Android 省电策略延后；保留期限是逻辑到期时间，不保证精确到分钟执行。
- 自动管理依赖设备支持及用户授予的媒体管理特殊权限；如果系统操作未完成，APP 会保留图片状态并提示检查。

## 构建

使用 Android Studio 的 Embedded JDK，或在终端配置 JDK 后运行：

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

发布 APK 可在 Android Studio 中通过 `Build` → `Generate Signed Bundle or APK` → `APK` 创建。

---

# TempShot

A privacy-first native Android app for managing temporary photos locally. It does not connect to the internet or upload photos. New photos can receive a retention period and expired items are handled through the system trash.

## Release

Download the release APK: [`releases/TempShot-0.2.5-release.apk`](releases/TempShot-0.2.5-release.apk)

Android 12 (API 31) or later is required. Uninstall a previously installed debug build before installing this signed release build.

## Highlights

- Retention options from 15 minutes to one week.
- Automatic enrollment for camera photos, screenshots, and images saved by other apps.
- Manual cleanup with user confirmation, or permission-gated best-effort automatic system-trash cleanup.
- Chinese and English interface.
- On-device processing only; no network permission, photo upload, or cloud sync.

## Notes

Background checks run no more frequently than every 15 minutes and can be delayed by Android battery optimization. Automatic cleanup requires full photo access and media-management permission, and behavior can vary by device.
