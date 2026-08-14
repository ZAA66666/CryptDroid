package com.zaa.cryptdroid.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.zaa.cryptdroid.security.VaultStore
import com.zaa.cryptdroid.ui.components.PwaButton
import com.zaa.cryptdroid.ui.components.PwaFieldLabel
import com.zaa.cryptdroid.ui.components.PwaResultBox
import com.zaa.cryptdroid.ui.components.PwaScreen
import com.zaa.cryptdroid.ui.components.PwaTextField

/**
 * SettingsScreen — 设置页（原版风格）
 * 主密码维护 + 关于
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vault = remember { VaultStore(context) }

    var newPwd by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var oldPwd by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val initialized = remember { vault.isMasterPasswordSet() }

    PwaScreen(title = "设置", onBack = onBack) {
        PwaFieldLabel("密码本状态")
    Text(
        if (initialized) "✅ 已设置主密码，密码本数据已加密保护"
        else "⚠️ 尚未设置主密码（首次使用请先设置）",
        color = if (initialized) Color(0xFF00A862) else Color(0xFFE53935)
    )

    if (!initialized) {
        PwaFieldLabel("设置主密码")
        Text("⚠️ 主密码不可恢复！遗忘将永久丢失密码本数据。", color = Color(0xFFE53935))
        PwaTextField(newPwd, { newPwd = it }, "设置主密码")
        PwaButton("保存") {
            if (newPwd.length < 4) {
                status = "密码至少 4 位"
            } else {
                runCatching { vault.createMasterPassword(newPwd) }
                    .onSuccess { status = "✅ 主密码已设置，密码本已加密" }
                    .onFailure { status = "设置失败: ${it.message}" }
            }
        }
    } else {
        PwaFieldLabel("解锁密码本")
        PwaTextField(pwd, { pwd = it }, "输入主密码")
        PwaButton("解锁") {
            status = if (vault.unlock(pwd)) "✅ 密码正确，密码本已解锁" else "❌ 密码错误"
            pwd = ""
        }

        PwaFieldLabel("修改主密码")
        PwaTextField(oldPwd, { oldPwd = it }, "旧密码")
        PwaTextField(newPwd, { newPwd = it }, "新密码")
        PwaButton("修改密码") {
            status = if (vault.changeMasterPassword(oldPwd, newPwd)) {
                "✅ 密码已修改，数据已重新加密"
            } else {
                "❌ 修改失败（旧密码错误或密码本为空）"
            }
            oldPwd = ""; newPwd = ""
        }
    }

    PwaResultBox("状态", status)

    PwaFieldLabel("关于")
    Text("CryptDroid v${com.zaa.cryptdroid.BuildConfig.VERSION_NAME}", color = Color(0xFF1A1A1A))
    Text("离线加密工具箱：哈希 / 编解码 / 加解密 / 二维码 / JSON / 随机 / 文本 / Crontab", color = Color(0xFF8A8F96))
    Text("全部功能离线运行，数据不出本机。", color = Color(0xFF8A8F96))
    Text("开源库：Jetpack Compose / ZXing / BouncyCastle，见 THIRD_PARTY_NOTICES", color = Color(0xFF8A8F96))
    }
}
