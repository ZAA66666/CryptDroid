# 交接文档：CryptDroid（哈机码）Kotlin + Compose 原生版作战计划

> **本文档 = 任务交接书 + 开发作战计划**。
> 给任何新 AI / 新会话看这一份即可开工，无需翻历史对话。
> 配套参考：`DEV_DOC_JAVA.md`（功能规格全文）、`AI_PROMPT_JAVA.md`（可整体投喂给 AI 的提示词）、现有 PWA 版源码 `D:\Project\workbuddy\crypto-pwa\`（视觉与交互参照）。

---

## 0. 一句话总结

把现有 **PWA + Capacitor** 版加密工具箱「哈机码」，用 **Kotlin + Jetpack Compose 原生 Android** 从零复刻，APK 体积从 **11MB 降到 ≤6MB**，全离线运行，已搭好可编译骨架 + 云端出包 CI。

> **技术栈决策（彭总 2026-08-14 拍板）**：不再用 Capacitor/PWA 跨平台框架——因其实现无法让彭总建立"能完成任务"的信心。改用 **Kotlin + Jetpack Compose**（更简洁高效的原生方案）。

---

## 1. 两个"版"的关系（务必分清）

| | 现有版（参考用） | 本次交接版（新项目） |
|---|---|---|
| 目录 | `D:\Project\workbuddy\crypto-pwa` | `D:\Project\workbuddy\cryptpwa-native` |
| 技术 | PWA + Capacitor 8（JS 全家桶） | **Kotlin + Jetpack Compose** |
| 体积 | ~11MB（含 WebView） | 目标 ≤6MB |
| 状态 | 功能完整 v1.3.0，CI 正常出包 | **骨架已搭（Compose 主页 + native 安全层），待逐页填充** |
| 包名 | `com.zaa.cryptdroid` | `com.zaa.cryptdroid`（一致） |
| 语言 | JS（app.js 2107 行等） | Kotlin（Compose 声明式 UI） |

> ⚠️ **不共享代码**，是两套独立工程。原生版是"参照 PWA 版功能做复刻"，不是改造现有版。

---

## 2. 本机环境（重要约束）

- **无 JDK、无 Android SDK、无 JAVA_HOME** → 本机无法直接 `gradlew assembleDebug`
- **构建唯一路径 = GitHub Actions 云端出包**（项目已配好 workflow）
- Gradle 版本沿用 Capacitor 版：**Gradle 8.14.3 + AGP 8.13.0 + Java 21**（wrapper jar 已复制）
- 用户机器：Win11，C 盘紧张，工程代码一律放 `D:\Project`

---

## 3. 已就位的项目骨架（本次交付）

```
cryptpwa-native/
├── settings.gradle                  # 仅 app 模块
├── build.gradle                     # AGP 8.13.0
├── gradle.properties                # AndroidX + 并行 + 缓存
├── gradle/wrapper/                  # gradle-wrapper.jar + properties（已复制）
├── gradlew / gradlew.bat            # 已复制
├── .gitignore                       # 忽略 p12 签名/构建产物
├── .github/workflows/build-apk.yml  # CI：安装 NDK → 云端出 debug/release APK + 发 Release
└── app/
    ├── build.gradle                 # Kotlin + Compose、minify 关、条件签名、CMake/native
    └── src/main/
        ├── AndroidManifest.xml
        ├── cpp/                     # ★ native 安全层（C + CMake）
        │   ├── CMakeLists.txt       #    编译 crypto_native.so（-Os 体积优化）
        │   └── crypto_native.c      #    PBKDF2 派生 + AES-256-GCM + 主密码校验
        ├── java/com/zaa/cryptdroid/
        │   ├── MainActivity.kt      # Compose 主页：TopAppBar + 9 工具卡片（LazyColumn）+ native 自检
        │   ├── model/ToolItem.kt    # 工具模型（图标/颜色/标题）
        │   ├── ui/theme/            # Compose 主题：Theme.kt + AppIcons.kt（手写图标，零 extra 依赖）
        │   └── security/            # ★ 安全封装（Kotlin）
        │       ├── NativeCrypto.kt  #   JNI 桥（派生/加解密/校验）
        │       └── VaultStore.kt    #   密码本存储协议（主密码维护）
        └── res/
            ├── values/strings.xml colors.xml themes.xml
            └── mipmap*/（自适应图标 + 兜底图标）
```

**骨架能干什么**：
- 启动 App → 显示 TopAppBar + 9 个工具卡片列表（Compose Material3，酷安绿 `#00a862` 主题，浅/深色自适应）
- 点击工具 Toast 提示（骨架占位）
- **native 安全层已就位**：so 加载 + 主密码派生 + AES-GCM 加解密 + 主密码校验全链路可在 debug 自检中验证

---

## 4. 已安装的 Java/Kotlin 技能与分工（核心编排）

> ⚠️ 技术栈为 Kotlin + Compose，但 Kotlin 是 JVM 语言，以下 Java 技能的**架构/规范/工程**部分完全适用；`java-expert` 侧重 Java 源码分析，编写 Kotlin 时作为参考即可。

以下技能都已装在 `~/.workbuddy/skills/`，开发时按需加载：

| 技能 | 负责环节 | 使用时机 |
|---|---|---|
| `java-architect` | 整体架构、分层设计、模块划分 | 开工前先加载，定包结构 |
| `java-expert` | 具体功能编写、算法实现、代码生成 | 每个页面/功能开发时（Kotlin 语义自判） |
| `java-development-manual` | **代码规范审查**（命名/异常/安全/工程结构） | 每完成一个大块后自查 |
| `android-native-dev` | Android 专属：Compose UI、构建排障、Material 3 | UI 开发与编译报错时 |
| `dev-discipline` | 质量守卫：先分析根因再动手，重构优于打补丁 | 全程，尤其修 bug 时 |
| `fullstack-dev` | （参考）工程化思想，非必须 | 可选 |

**标准工作流**（每次开发任务都走这条链）：
```
任务 → java-architect 定方案 → java-expert 写代码 → java-development-manual 审查规范
     → android-native-dev 查 UI/构建 → 自查闭环 → 交 CI 出包验证
```

---

## 4.5 安全架构（彭总强制要求，违反即返工）

> 需求原文：「有主密码需要维护本地密码信息……app 尽量难以被破解」

### 4.5.1 分层保护原则

| 层 | 内容 | 抗破解等级 |
|---|---|---|
| **native（.so）** | 主密码派生 PBKDF2、密码本 AES-256-GCM 加解密、主密码校验 | 高（ARM 机器码，反汇编难） |
| **Kotlin** | 哈希/编解码/JSON 等无保密价值工具 | 中（字节码可反编译，但无敏感数据） |
| **存储** | 密码本文件 `vault_data.bin` = iv‖密文+tag；元数据 `vault_meta.bin` = 盐+迭代+指纹 | 密文态，离线不可读 |

### 4.5.2 主密码协议（VaultStore 已实现）

```
设置主密码：salt=随机32字节 → fingerprint=PBKDF2(pwd,salt,10000,32B)
            → 存 salt+iter+fingerprint（主密码本身永不落盘）
校验主密码：verifyPassword(pwd,salt,iter,fingerprint) → constant-time 比较
加密数据  ：key=PBKDF2(pwd,salt,iter,32B) → AES-256-GCM(key,新iv,JSON)
存储格式  ：vault_data.bin = iv(12) ‖ 密文+tag(16)
```

### 4.5.3 抗破解手段清单（已落地）

1. **核心逻辑在 native**：`.so` 是 ARM 机器码，`-fvisibility=hidden` 隐藏符号，逆向需 IDA/Ghidra 反汇编
2. **密钥不落 Kotlin 堆**：密钥只在 C 栈/堆中短暂存在，`secure_wipe()` 用完即清
3. **主密码不落盘**：只存不可逆指纹，暴力破解只能靠 PBKDF2 10000 次迭代拖慢（每次尝试 ≈ 数十毫秒）
4. **constant-time 校验**：`CRYPTO_memcmp` 防时序侧信道
5. **GCM 认证**：密码错误/数据被篡改 → 解密直接返回 null，不泄露任何明文
6. **系统 BoringSSL**：动态链接 `-lcrypto`（系统自带），不打包第三方加密库，零体积成本 + 算法正确性有保障
7. **固定签名**：防重打包篡改（后续 release 配置）

### 4.5.4 性能与流畅度规范

- 加密/解密/派生一律放**后台线程**（`ExecutorService`），主线程只做 UI
- PBKDF2 迭代 10000 次 ≈ 数十毫秒，解锁时显示进度提示，避免误以为卡死
- Compose LazyColumn 自带 Item 复用（已实现）；`items(key=...)` 稳定键避免重组闪烁
- 大文本处理（哈希大文件等）流式分块，不一次性载入内存

### 4.5.5 体积控制

- 双 ABI：`armeabi-v7a` + `arm64-v8a`（兼容 32 位，由彭总确认）
- native 链接系统 BoringSSL（`libcrypto.so` 系统自带，APK 零增加）
- 不引入 Kotlin/Compose/WebView；依赖仅 appcompat/material/constraintlayout/recyclerview
- release 关闭 R8 minify（保守，防加密库反射被裁），靠 abiFilters + 资源压缩控体积

---

## 5. 分步开发计划（里程碑）

按此顺序推进，每步保证"能编译"再进下一步：

- [x] **M0 骨架**（本次完成）：Gradle 工程 + 主页 9 工具列表 + CI 出包链
- [x] **M0.5 安全层**（本次完成）：native .so（PBKDF2 + AES-GCM + 主密码校验）+ VaultStore 密码本协议 + CI 装 NDK
- [ ] **M1 设置页 + 导航**：Toolbar 三点菜单 → 设置页（语言/主题/强调色/字体）；工具点击跳转对应页
- [ ] **M2 哈希 + 编解码**：MD5/SHA 系列/HMAC/SHA3-Keccak；Base64/32/58/Hex/URL/Unicode/JWT/HTML
- [ ] **M3 加解密（对称）**：AES/DES/3DES/Blowfish/RC4/Rabbit × 5 模式，IV/密钥/密码本
- [ ] **M4 加解密（非对称）**：RSA 2048（4 操作）+ SM2 国密（C1C3C2）
- [ ] **M5 二维码/条形码**：生成 + 摄像头扫描（ZXing）
- [ ] **M6 JSON + 随机 + 文本工具**：格式化/键值编辑；随机串/虚假数据；统计/去重/diff
- [ ] **M7 Crontab + 教程 + 历史**：5 段解析/下次执行；10 张教程卡；最近使用记录
- [ ] **M8 数据存储收尾**：设置 SharedPreferences、WebDAV 备份、密码本 UI 接入 VaultStore
- [ ] **M9 收尾**：i18n 中英、体积压测、release 签名、发布

---

## 6. 构建与出包（关键路径）

### 6.1 本地
```bash
cd D:/Project/workbuddy/cryptpwa-native
./gradlew :app:assembleDebug   # 需要 JDK 21 + Android SDK（本机暂无）
```

### 6.2 GitHub Actions（本机唯一可行路径）
1. 推送到 GitHub 仓库 main 分支（或手动触发 workflow）
2. `./gradlew :app:assembleDebug`（push 默认）→ 自动发 Release 附 APK
3. 手动运行选 `release` → 需先配置 Secret：`CRYPTPWA_NATIVE_P12`（base64 的 p12 签名文件）

### 6.3 签名
- 骨架 release 用**条件签名**：`app/cryptpwa-release.p12` 存在才签名
- 签名固定（PKCS12，alias=cryptpwa / 密码 cryptpwa2026，与 PWA 版一致，覆盖安装互通）
- 生成签名文件命令（本机有 keytool 时执行）：
```bash
keytool -genkeypair -v -keystore cryptpwa-release.p12 -storetype PKCS12 \
  -alias cryptpwa -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass cryptpwa2026 -keypass cryptpwa2026 \
  -dname "CN=CryptPwa, OU=dev, O=zaa, L=GZ, ST=GD, C=CN"
```

---

## 7. 硬性约束（违反即返工）

1. **Kotlin + Compose**，UI 全部用 Compose 声明式（不用 XML/RecyclerView）；native 层保持 C（CMake）
2. **所有计算本地完成**，无网络依赖（仅 WebDAV 备份可选联网）
3. **minSdk 26 / targetSdk 34**，包名 `com.zaa.cryptdroid`
4. **版本号**：versionName 形如 `v{YYYYMMDD}_{HHmm}`，versionCode 单调递增
5. **体积目标 ≤6MB**（不依赖 WebView；依赖为 Compose BOM + material3 + core-ktx + activity-compose；**加密用系统 BoringSSL 不引 BouncyCastle；不引 icons-extended**）
6. **release 不开 R8 minify**（保守，防加密库反射被裁）
7. **i18n**：字符串走资源文件，中英双语，不硬编码中文
8. **状态栏**：主页深色背景白字，功能页浅色背景深字，随页面切换
9. **主密码逻辑必须走 native**：派生/校验/加解密一律经 `NativeCrypto`，禁止在 Kotlin 侧自行实现 PBKDF2 或用弱算法存密码本
10. **主密码永不落盘**：本地只存指纹（salt+iter+fingerprint），遗忘不可恢复
11. **密钥用完即清**：ByteArray 用完 `fill(0)`，不缓存不打印
12. **双 ABI**：`armeabi-v7a` + `arm64-v8a`（兼容 32 位），不改回仅 64 位
13. **不引入 material-icons-extended**（2000+ 图标整包打入，体积爆炸）；工具图标用 `AppIcons.kt` 手写 path

---

## 8. 易踩坑点（DEV_DOC_JAVA 提炼 + native 补充）

| # | 坑 | 说明 |
|---|---|---|
| 1 | SHA3 ≠ Keccak | FIPS 202 标准 SHA3-512 与 crypto-js 的原始 Keccak 输出不同，需两个独立实现 |
| 2 | SM2 密文 C1C3C2 | 国密标准顺序，十六进制输出 |
| 3 | RSA 单次加密上限 | 2048 位 ≈ 190 字节明文，长文本需分段 |
| 4 | 中文字节 | UTF-8 下一汉字 3 字节，密钥/IV 字节校验按 UTF-8 算 |
| 5 | 主密码不可恢复 | 首次设置需明确提示，无服务器备份 |
| 6 | 自适应图标 | anydpi-v26 + mipmap 兜底 vector 都已放好，别删 |
| 7 | **NDK/CMake 版本一致性** | build.gradle `ndkVersion 27.2.12479018` 必须与 CI `setup-android` 的 `ndk-version` 一致，否则云端编译失败 |
| 8 | **GCM 的 iv 必须每次重新随机** | 复用 iv 会破坏 GCM 安全性，`savePlainData` 已强制 `SecureRandom` 生成新 iv |
| 9 | **AES-GCM 密钥必须 32 字节 / iv 12 字节** | native 层强校验，传错直接抛 IllegalArgumentException，调用方勿改 |
| 10 | **BoringSSL 链接** | CMake `target_link_libraries(crypto_native android log)` + `-lcrypto`（系统库）；本地构建需 ANDROID_NDK，CI 用 setup-android 装 |
| 11 | **native 方法名不可改** | JNI 函数名 = Java 包名+类名+方法名拼接；Kotlin object 的 external 方法是实例方法（JNI 第二参数是 jobject 非 jclass，C 层已对应修改）；改包名/类名/方法名必须同步改 crypto_native.c，否则 UnsatisfiedLinkError |
| 12 | **Compose 图标库体积** | 禁止引入 material-icons-extended；用 AppIcons.kt 手写 ImageVector（material3 自带 icons-core 已有 Menu/Settings/Lock 等） |
| 13 | **Compose BOM 版本** | 依赖用 `compose-bom` 统一管理，勿手写各 artifact 版本（否则版本冲突） |

---

## 9. 下一步做什么（交接给下一个 AI）

> 彭总可直接说：**"按交接文档 M1 继续，开发设置页"** 即可开工。

推荐首个完整任务：**M1 设置页 + 工具页跳转**——让骨架真正"活"起来（点击工具能进对应页面、设置能切主题/语言），这一步能验证整个导航架构，后续功能页照此模式批量填充。

---

## 10. 变更记录

| 时间 | 内容 |
|---|---|
| 2026-08-14 | 交接文档创建；M0 骨架完成（Gradle 工程 + 主页列表 + CI 出包链） |
| 2026-08-14 | **M0.5 安全层完成**：native .so（PBKDF2 + AES-256-GCM + 主密码校验）+ VaultStore 密码本协议 + CI 装 NDK；新增 4.5 安全架构章节 |
| 2026-08-14 | **技术栈切换为 Kotlin + Jetpack Compose**（彭总拍板弃用 Capacitor/PWA）；主页改 Compose LazyColumn + Material3 主题；图标改手写 AppIcons.kt（防体积爆炸）；native 层保留；仓库更名 CryptDroid |
