# ProGuard/R8 规则（当前 minify 关闭，此文件为将来开启 R8 做准备）

# --- Kotlin ---
# Kotlin 反射/协程（若启用）
-dontwarn kotlin.**

# --- Compose ---
# Compose 编译器生成的代码由 Gradle 插件管理，无需额外 keep

# --- Native (JNI) ---
# JNI 方法名由 C 层硬编码，绝不能混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# --- BouncyCastle（如将来引入）---
# -keep class org.bouncycastle.** { *; }
# -dontwarn org.bouncycastle.**
