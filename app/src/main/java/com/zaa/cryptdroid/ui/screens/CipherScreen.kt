package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zaa.cryptdroid.ui.components.PwaButton
import com.zaa.cryptdroid.ui.components.PwaFieldLabel
import com.zaa.cryptdroid.ui.components.PwaGhostButton
import com.zaa.cryptdroid.ui.components.PwaResultBox
import com.zaa.cryptdroid.ui.components.PwaScreen
import com.zaa.cryptdroid.ui.components.PwaTextField
import com.zaa.cryptdroid.util.CryptoUtil

/**
 * CipherScreen — 加/解密工具页（原版风格）
 * 对称 + RSA + SM2
 */
@Composable
fun CipherScreen(onBack: () -> Unit) {
    PwaScreen(title = "加/解密", onBack = onBack) {
        var category by remember { mutableStateOf("对称") }
        var text by remember { mutableStateOf("") }
        var keyText by remember { mutableStateOf("") }
        var ivText by remember { mutableStateOf("") }
        var algo by remember { mutableStateOf("AES") }
        var mode by remember { mutableStateOf("CBC") }
        var action by remember { mutableStateOf("encrypt") }
        var result by remember { mutableStateOf("") }
        var keyPair by remember { mutableStateOf("" to "") }

        PwaFieldLabel("类型")
        Row(verticalAlignment = Alignment.CenterVertically) {
            listOf("对称", "RSA", "SM2").forEach { label ->
                RadioButton(selected = category == label, onClick = { category = label })
                Text(label, color = Color(0xFF1A1A1A))
            }
        }

        PwaFieldLabel("输入")
        PwaTextField(text, { text = it }, "明文 / 密文（Hex）", minLines = 3)

        when (category) {
            "对称" -> {
                PwaFieldLabel("密钥（UTF-8）")
                PwaTextField(keyText, { keyText = it }, "密钥")
                PwaFieldLabel("IV 偏移（可选，ECB/流式忽略）")
                PwaTextField(ivText, { ivText = it }, "IV 偏移")
            }
            "RSA" -> {
                PwaFieldLabel(if (action == "encrypt") "公钥 PEM" else "私钥 PEM")
                PwaTextField(keyText, { keyText = it }, "PEM 密钥", minLines = 4)
            }
            "SM2" -> {
                PwaFieldLabel(if (action == "encrypt") "公钥 Hex" else "私钥 Hex")
                PwaTextField(keyText, { keyText = it }, "Hex 密钥")
            }
        }

        if (category == "对称") {
            PwaFieldLabel("算法与模式")
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CryptoUtil.SYMMETRIC_ALGOS.forEach { (a, _) ->
                        RadioButton(selected = algo == a, onClick = { algo = a })
                        Text(a, color = Color(0xFF1A1A1A))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CryptoUtil.MODES.forEach { m ->
                        RadioButton(selected = mode == m, onClick = { mode = m })
                        Text(m, color = Color(0xFF1A1A1A))
                    }
                }
            }
        }

        PwaFieldLabel("操作")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = action == "encrypt", onClick = { action = "encrypt" })
            Text("加密", color = Color(0xFF1A1A1A))
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            RadioButton(selected = action == "decrypt", onClick = { action = "decrypt" })
            Text("解密", color = Color(0xFF1A1A1A))
        }

        PwaButton("执行") {
            result = runCatching {
                when (category) {
                    "对称" -> {
                        val key = keyText.toByteArray(Charsets.UTF_8)
                        val iv = if (ivText.isNotBlank()) ivText.toByteArray(Charsets.UTF_8) else null
                        val out = if (action == "encrypt") {
                            CryptoUtil.symmetricEncrypt(algo, mode, key, iv, text.toByteArray(Charsets.UTF_8))
                        } else {
                            CryptoUtil.symmetricDecrypt(algo, mode, key, iv, CryptoUtil.hexToBytes(text))
                        }
                        out.toHex()
                    }
                    "RSA" -> {
                        if (action == "encrypt") {
                            CryptoUtil.rsaEncrypt(keyText, text.toByteArray(Charsets.UTF_8)).toHex()
                        } else {
                            String(CryptoUtil.rsaDecrypt(keyText, CryptoUtil.hexToBytes(text)))
                        }
                    }
                    "SM2" -> {
                        if (action == "encrypt") {
                            CryptoUtil.sm2Encrypt(keyText, text.toByteArray(Charsets.UTF_8))
                        } else {
                            String(CryptoUtil.sm2Decrypt(keyText, text))
                        }
                    }
                    else -> "未知类型"
                }
            }.getOrElse { e -> "错误: ${e.message}" }
        }

        if (category != "对称") {
            PwaGhostButton("生成密钥对") {
                keyPair = if (category == "RSA") CryptoUtil.generateRsaKeyPair() else CryptoUtil.generateSm2KeyPair()
            }
            if (keyPair.first.isNotEmpty()) {
                PwaResultBox("密钥对（请复制保存）", "公钥:\n${keyPair.first}\n\n私钥:\n${keyPair.second}", placeholder = "点击上方生成")
            }
        }

        PwaResultBox("结果（Hex）", result)
    }
}
