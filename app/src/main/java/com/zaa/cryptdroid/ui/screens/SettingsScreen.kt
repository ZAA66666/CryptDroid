package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zaa.cryptdroid.security.VaultStore
import com.zaa.cryptdroid.ui.components.ActionButtonRow
import com.zaa.cryptdroid.ui.components.ResultBox
import com.zaa.cryptdroid.ui.components.SectionCard
import com.zaa.cryptdroid.ui.components.ToolScaffold

/**
 * SettingsScreen — 设置页
 * 主密码维护（设置/解锁/修改）+ 关于
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vault = remember { VaultStore(context) }

    var newPwd by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var oldPwd by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    // 初始状态
    val initialized = remember { vault.isMasterPasswordSet() }

    SectionCard(title = "密码本状态") {
        Text(
            if (initialized) "✅ 已设置主密码，密码本数据已加密保护"
            else "⚠️ 尚未设置主密码（首次使用请先设置）",
            style = MaterialTheme.typography.bodyMedium,
            color = if (initialized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }

    if (!initialized) {
        SectionCard(title = "设置主密码") {
            Text("⚠️ 主密码不可恢复！遗忘将永久丢失密码本数据。", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newPwd,
                onValueChange = { newPwd = it },
                label = { Text("设置主密码") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        ActionButtonRow(listOf(
            "保存" to {
                if (newPwd.length < 4) {
                    status = "密码至少 4 位"
                } else {
                    runCatching { vault.createMasterPassword(newPwd) }
                        .onSuccess { status = "✅ 主密码已设置，密码本已加密" }
                        .onFailure { status = "设置失败: ${it.message}" }
                }
            }
        ))
    } else {
        SectionCard(title = "解锁密码本") {
            OutlinedTextField(
                value = pwd,
                onValueChange = { pwd = it },
                label = { Text("输入主密码") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        ActionButtonRow(listOf(
            "解锁" to {
                status = if (vault.unlock(pwd)) "✅ 密码正确，密码本已解锁" else "❌ 密码错误"
                pwd = ""
            }
        ))

        SectionCard(title = "修改主密码") {
            OutlinedTextField(
                value = oldPwd,
                onValueChange = { oldPwd = it },
                label = { Text("旧密码") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newPwd,
                onValueChange = { newPwd = it },
                label = { Text("新密码") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        ActionButtonRow(listOf(
            "修改密码" to {
                status = if (vault.changeMasterPassword(oldPwd, newPwd)) {
                    "✅ 密码已修改，数据已重新加密"
                } else {
                    "❌ 修改失败（旧密码错误或密码本为空）"
                }
                oldPwd = ""; newPwd = ""
            }
        ))
    }

    ResultBox(title = "状态", text = status)

    SectionCard(title = "关于") {
        Text("CryptDroid v${com.zaa.cryptdroid.BuildConfig.VERSION_NAME}")
        Text("离线加密工具箱：哈希 / 编解码 / 加解密 / 二维码 / JSON / 随机 / 文本 / Crontab")
        Text("全部功能离线运行，数据不出本机。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text("开源库：Jetpack Compose / ZXing / BouncyCastle，见 THIRD_PARTY_NOTICES", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
