package com.zaa.cryptdroid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaa.cryptdroid.model.ToolItem
import com.zaa.cryptdroid.security.NativeCrypto
import com.zaa.cryptdroid.ui.Screen
import com.zaa.cryptdroid.ui.screens.CipherScreen
import com.zaa.cryptdroid.ui.screens.CrontabScreen
import com.zaa.cryptdroid.ui.screens.EncodeScreen
import com.zaa.cryptdroid.ui.screens.GuideScreen
import com.zaa.cryptdroid.ui.screens.HashScreen
import com.zaa.cryptdroid.ui.screens.JsonScreen
import com.zaa.cryptdroid.ui.screens.QrScreen
import com.zaa.cryptdroid.ui.screens.RandomScreen
import com.zaa.cryptdroid.ui.screens.SettingsScreen
import com.zaa.cryptdroid.ui.screens.TextToolScreen
import com.zaa.cryptdroid.ui.theme.CryptDroidTheme

/* 原版色板 */
private val Accent = Color(0xFF00A862)
private val AccentSoft = Color(0xFFE6F6EE)
private val PageBg = Color(0xFFF5F6F8)
private val CardBg = Color(0xFFFFFFFF)
private val TextMain = Color(0xFF1A1A1A)
private val TextMuted = Color(0xFF8A8F96)
private val Border = Color(0xFFE3E5E8)

/**
 * MainActivity — 应用入口 + 页面导航（Jetpack Compose）
 * UI 风格对齐原 PWA 版（crypto-pwa）：绿顶栏 + 分组卡片工具列表。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptDroidTheme {
                AppRoot()
            }
        }

        if (BuildConfig.DEBUG) {
            selfTestNative()
        }
    }

    /** native 层自检：派生密钥 → 加密 → 解密 → 主密码校验 */
    private fun selfTestNative() {
        try {
            val salt = ByteArray(16) { 7 }
            val key = NativeCrypto.deriveKey("123456".toByteArray(), salt, 1000, 32)
            val packed = NativeCrypto.aesGcmEncrypt(key, null, "自检数据".toByteArray())
            val plain = NativeCrypto.aesGcmDecrypt(key, packed)
            if (plain == null || "自检数据" != plain.toString(Charsets.UTF_8)) {
                throw IllegalStateException("decrypt 往返失败")
            }
            val wrongPwd = NativeCrypto.verifyPassword("wrong".toByteArray(), salt, 1000, key)
            val tampered = packed.copyOf().also { it[it.size - 1] = (it[it.size - 1] + 1).toByte() }
            if (wrongPwd) throw IllegalStateException("verify 误判")
            if (NativeCrypto.aesGcmDecrypt(key, tampered) != null) throw IllegalStateException("GCM 未拦截篡改")
            Log.i("NativeSelfTest", "✅ native 自检通过")
        } catch (t: Throwable) {
            Log.e("NativeSelfTest", "❌ native 自检失败", t)
        }
    }
}

/** 应用根组件：页面状态切换 */
@Composable
fun AppRoot() {
    var current by remember { mutableStateOf(Screen.HOME) }
    when (current) {
        Screen.HOME -> MainScreen(onNavigate = { current = it })
        Screen.HASH -> HashScreen(onBack = { current = Screen.HOME })
        Screen.ENCODE -> EncodeScreen(onBack = { current = Screen.HOME })
        Screen.CIPHER -> CipherScreen(onBack = { current = Screen.HOME })
        Screen.QR -> QrScreen(onBack = { current = Screen.HOME })
        Screen.JSON -> JsonScreen(onBack = { current = Screen.HOME })
        Screen.RANDOM -> RandomScreen(onBack = { current = Screen.HOME })
        Screen.TEXT -> TextToolScreen(onBack = { current = Screen.HOME })
        Screen.CRONTAB -> CrontabScreen(onBack = { current = Screen.HOME })
        Screen.GUIDE -> GuideScreen(onBack = { current = Screen.HOME })
        Screen.SETTINGS -> SettingsScreen(onBack = { current = Screen.HOME })
    }
}

/** 主页：原版绿顶栏 + 分组卡片工具列表 */
@Composable
fun MainScreen(onNavigate: (Screen) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(PageBg)) {
        // 绿顶栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Accent)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_title),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "菜单", tint = Color.White)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_settings)) },
                        onClick = { menuExpanded = false; onNavigate(Screen.SETTINGS) },
                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) }
                    )
                }
            }
        }

        // 工具列表（分组卡片）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
            ) {
                val tools = ToolItem.all()
                tools.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border))
                    }
                    ToolRow(item = item, onClick = { onNavigate(item.screen) })
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.home_subtitle),
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/** 原版工具行：浅绿图标块 + 粗体标题 + 斜体描述 + ›箭头 */
@Composable
fun ToolRow(item: ToolItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标块：36px 圆角10 浅绿底
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.titleRes),
                color = TextMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(item.descRes),
                color = TextMuted,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic
            )
        }
        Text("›", color = Accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
