# CryptDroid — 离线加密工具箱（Android 原生）

> 加密/编码/哈希/二维码/JSON/Cron 一站式离线工具箱。
> **Kotlin + Jetpack Compose** 原生实现，纯本地计算，无任何网络依赖。

## 特性

- 🧮 **哈希**：MD5 / SHA-1/256/512 / HMAC / SHA3 / Keccak
- 🔀 **编/解码**：Base64 / Base32 / Base58 / Hex / URL / Unicode / JWT / HTML
- 🔐 **加/解密**：AES / DES / 3DES / Blowfish / RC4 / Rabbit + RSA / SM2 国密
- 📱 **二维码/条形码**：生成（含 Logo 美化）+ 摄像头扫描
- 🧩 **JSON**：格式化 / 压缩 / 校验 / 键值编辑
- 🎲 **随机文本**：随机字符串 + 虚假数据生成
- 📝 **文本工具**：字数统计 / 去重 / 文本对比
- ⏰ **Crontab**：5 段表达式解析 + 下次执行时间
- 🔑 **密码本**：主密码 + AES-256-GCM 加密存储（核心逻辑在 native 层）

## 安全设计

- **主密码派生与密码本加解密全部下沉 native 层**（`crypto_native.so`），ARM 机器码难以逆向
- 主密码永不落盘，本地只存不可逆指纹（PBKDF2-HMAC-SHA256，10000 次迭代）
- 密钥只在 C 堆内存短暂存在，用完即清（`secure_wipe`）
- 密码错误/数据篡改 → GCM 认证失败 → 返回 null，不泄露任何明文
- 链接系统 BoringSSL（`-lcrypto`），APK 体积零增加

## 体积目标

- APK ≤ 6MB（Kotlin + Compose 原生，无 WebView）
- 仅 arm64-v8a + armeabi-v7a 双 ABI
- 不引入 material-icons-extended / BouncyCastle，图标手写 ImageVector

## 开发文档

- **`HANDOVER.md`** — 交接文档 / 作战计划（新 AI 先读这份）
- **`DEV_DOC_JAVA.md`** — 功能规格全文（原目录 `D:\Project\workbuddy\crypto-pwa`）

## 构建

### 本地
```bash
./gradlew :app:assembleDebug   # 需 JDK 21 + Android SDK + NDK
```

### GitHub Actions（推荐）
推送 main 分支自动构建 debug APK 并发布 Release。

## License

仅供个人学习使用。
