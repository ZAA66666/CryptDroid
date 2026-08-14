package com.zaa.cryptdroid.ui

/**
 * AppNavigation — 页面导航模型
 * 采用轻量状态切换（不引入 Navigation-Compose，省体积，符合 ≤6MB 目标）。
 */
enum class Screen {
    HOME,
    HASH,
    ENCODE,
    CIPHER,
    QR,
    JSON,
    RANDOM,
    TEXT,
    CRONTAB,
    GUIDE,
    SETTINGS
}
