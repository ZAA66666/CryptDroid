package com.zaa.cryptdroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* 酷安绿主题色（与 PWA 版 #00a862 一致） */
private val AccentGreen = Color(0xFF00A862)
private val AccentGreenDark = Color(0xFF008C50)

/* 浅色配色方案 */
private val LightColors = lightColorScheme(
    primary = AccentGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBFF5DD),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = AccentGreenDark,
    onSecondary = Color.White,
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF6B6B6B)
)

/* 深色配色方案 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFF52D896),
    onPrimary = Color(0xFF00391C),
    primaryContainer = Color(0xFF005227),
    onPrimaryContainer = Color(0xFF9BF5C2),
    secondary = Color(0xFF8DE3B5),
    onSecondary = Color(0xFF00391C),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE4E4E4),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE4E4E4),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFF9E9E9E)
)

/**
 * CryptDroid 主题入口
 * 支持浅色/深色/跟随系统（由 isSystemInDarkTheme 控制）。
 * 后续可扩展：手动模式切换 + 自定义强调色（见 HANDOVER 设置页规划）。
 */
@Composable
fun CryptDroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
