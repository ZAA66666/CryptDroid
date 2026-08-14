# Third-Party Notices — 第三方开源库声明

本 App（CryptDroid）使用了以下开源项目。谨此致谢所有贡献者。

| 库 | 版本 | 用途 | License | 源码 |
|---|---|---|---|---|
| Jetpack Compose | BOM 2024.10.01 | UI 框架 | Apache 2.0 | https://github.com/androidx/androidx |
| material3 | (Compose BOM) | Material 3 组件 | Apache 2.0 | https://github.com/androidx/androidx |
| material-icons-core | (Compose BOM) | 图标 | Apache 2.0 | https://github.com/androidx/androidx |
| core-ktx | 1.13.1 | Kotlin 扩展 | Apache 2.0 | https://github.com/androidx/androidx |
| activity-compose | 1.9.3 | Compose Activity | Apache 2.0 | https://github.com/androidx/androidx |
| lifecycle-runtime-ktx | 2.8.7 | 生命周期 | Apache 2.0 | https://github.com/androidx/androidx |

## 计划引入（里程碑进行时补充）

| 库 | 用途 | License | 源码 |
|---|---|---|---|
| ZXing core | 二维码/条形码生成与扫描 (M5) | Apache 2.0 | https://github.com/zxing/zxing |
| BouncyCastle | SM2 国密等算法 (M4) | MIT | https://github.com/bcgit/bc-java |

## 说明

- **GPL/AGPL 协议库禁止引入**（传染性协议，会要求整个 App 开源）。
- 每次新增依赖，须同步更新本文件并在设置页「关于」中列出。
- Android 系统自带能力（javax.crypto 等）无需声明。

---

本文件依据各项目的开源许可证要求维护。
