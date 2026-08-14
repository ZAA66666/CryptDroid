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
import com.zaa.cryptdroid.ui.theme.CryptDroidTheme

/**
 * MainActivity — 主页（Jetpack Compose）
 *
 * 职责：
 *  1. TopAppBar（标题 + 三点菜单 → 设置/关于）
 *  2. 9 个工具卡片列表（LazyColumn 实现，流畅滚动）
 *  3. native 安全层自检（仅 debug）
 *
 * 与旧版（Java + XML）差异：使用 Compose 声明式 UI，无 RecyclerView/XML 布局。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptDroidTheme {
                MainScreen()
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
            // 1. 派生密钥（"123456" 主密码 + 固定盐）
            val salt = ByteArray(16) { 7 }
            val key = NativeCrypto.deriveKey("123456".toByteArray(), salt, 1000, 32)

            // 2. AES-GCM 加密（内部自动生成 iv，返回 iv‖密文+tag）
            val packed = NativeCrypto.aesGcmEncrypt(key, null, "自检数据".toByteArray())

            // 3. 解密（应还原原文）
            val plain = NativeCrypto.aesGcmDecrypt(key, packed)
            if (plain == null || "自检数据" != plain.toString(Charsets.UTF_8)) {
                throw IllegalStateException("decrypt 往返失败")
            }

            // 4. 错误密码应校验失败、篡改数据解密应返回 null（GCM 认证拦截）
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

/** 主页顶层组件 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

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
                                onClick = {
                                    menuExpanded = false
                                    Toast.makeText(context, R.string.placeholder_tip, Toast.LENGTH_SHORT).show()
                                },
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
                .padding(padding)
        )
    }
}

/** 9 个工具卡片列表（LazyColumn 滚动） */
@Composable
fun ToolList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tools = remember { ToolItem.all() }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tools, key = { it.id }) { item ->
            ToolCard(item = item, onClick = {
                // 骨架阶段：点击只提示，不跳转。后续替换为 Intent/Navigation 跳转。
                val label = context.getString(item.titleRes)
                Toast.makeText(context, "$label（${item.id}）开发中", Toast.LENGTH_SHORT).show()
            })
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
            // 彩色圆角图标块
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

            // 标题 + 描述
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

            // 右箭头
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
        MainScreen()
    }
}
