package com.zaa.cryptdroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

/**
 * MainActivity — 应用入口 + 页面导航（Jetpack Compose）
 *
 * 导航方式：轻量状态切换（Screen 枚举），不引入 Navigation 库，省体积。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptDroidTheme {
                AppRoot()
            }
        }

        // native 安全层自检：验证 so 加载 + 核心算法链路（仅 debug 显示）
        if (BuildConfig.DEBUG) {
            selfTestNative()
        }
    }

    /** native 层自检：派生密钥 → 加密 → 解密 → 主密码校验，全链路跑一遍 */
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
            val tamperedPlain = NativeCrypto.aesGcmDecrypt(key, tampered)

            if (wrongPwd) throw IllegalStateException("verify 误判")
            if (tamperedPlain != null) throw IllegalStateException("GCM 认证未拦截篡改")

            Log.i("NativeSelfTest", "✅ native 安全层自检通过：so 加载/派生/加解密/校验 全链路正常")
        } catch (t: Throwable) {
            Log.e("NativeSelfTest", "❌ native 自检失败", t)
            Toast.makeText(this, "原生安全层异常: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }
}

/** 应用根组件：管理当前页面状态 */
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

/** 主页：TopAppBar + 工具列表 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigate: (Screen) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
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
            )
        }
    ) { padding ->
        ToolList(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            onToolClick = { onNavigate(it) }
        )
    }
}

/** 9 个工具卡片列表（LazyColumn 滚动） */
@Composable
fun ToolList(modifier: Modifier = Modifier, onToolClick: (Screen) -> Unit) {
    val tools = remember { ToolItem.all() }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tools, key = { it.id }) { item ->
            ToolCard(item = item, onClick = { onToolClick(item.screen) })
        }
    }
}

/** 单个工具卡片：彩色圆角图标块 + 标题 + 副描述 + 右箭头 */
@Composable
fun ToolCard(item: ToolItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(item.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    CryptDroidTheme {
        MainScreen(onNavigate = {})
    }
}
