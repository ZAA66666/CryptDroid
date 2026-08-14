package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaa.cryptdroid.ui.components.ActionButtonRow
import com.zaa.cryptdroid.ui.components.ResultBox
import com.zaa.cryptdroid.ui.components.SectionCard
import com.zaa.cryptdroid.ui.components.ToolScaffold
import com.zaa.cryptdroid.util.CryptoUtil
import com.zaa.cryptdroid.util.toHex

/**
 * CipherScreen — 加/解密工具页
 * 对称（AES/DES/3DES/Blowfish/RC4/Rabbit）+ RSA + SM2
 */
@Composable
fun CipherScreen(onBack: () -> Unit) {
    ToolScaffold(title = "加/解密", onBack = onBack) { _ ->
        var category by remember { mutableStateOf("对称") } // 对称 / RSA / SM2
        var text by remember { mutableStateOf("") }
        var keyText by remember { mutableStateOf("") }
        var ivText by remember { mutableStateOf("") }
        var algo by remember { mutableStateOf("AES") }
        var mode by remember { mutableStateOf("CBC") }
        var action by remember { mutableStateOf("encrypt") }
        var result by remember { mutableStateOf("") }
        var keyPair by remember { mutableStateOf("" to "") }

        SectionCard(title = "类型") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf("对称", "RSA", "SM2").forEach { label ->
                    RadioButton(selected = category == label, onClick = { category = label })
                    Text(label)
                }
            }
        }

        SectionCard(title = "输入") {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("明文 / 密文（Hex）") },
                modifier = Modifier.fillMaxWidth()
            )
            when (category) {
                "对称" -> {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        label = { Text("密钥（UTF-8）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ivText,
                        onValueChange = { ivText = it },
                        label = { Text("IV 偏移（可选，ECB/流式忽略）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "RSA" -> {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        label = { Text(if (action == "encrypt") "公钥 PEM" else "私钥 PEM") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "SM2" -> {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        label = { Text(if (action == "encrypt") "公钥 Hex" else "私钥 Hex") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (category == "对称") {
            SectionCard(title = "算法与模式") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CryptoUtil.SYMMETRIC_ALGOS.forEach { (a, _) ->
                        RadioButton(selected = algo == a, onClick = { algo = a })
                        Text(a)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CryptoUtil.MODES.forEach { m ->
                        RadioButton(selected = mode == m, onClick = { mode = m })
                        Text(m)
                    }
                }
            }
        }

        SectionCard(title = "操作") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = action == "encrypt", onClick = { action = "encrypt" })
                Text("加密")
                Spacer(Modifier.height(0.dp))
                RadioButton(selected = action == "decrypt", onClick = { action = "decrypt" })
                Text("解密")
            }
        }

        ActionButtonRow(listOf(
            "执行" to {
                result = runCatching {
                    when (category) {
                        "对称" -> {
                            val key = keyText.toByteArray(Charsets.UTF_8)
                            val iv = if (ivText.isNotBlank()) ivText.toByteArray(Charsets.UTF_8) else null
                            val data = text.toByteArray(Charsets.UTF_8)
                            val out = if (action == "encrypt") {
                                CryptoUtil.symmetricEncrypt(algo, mode, key, iv, data)
                            } else {
                                CryptoUtil.symmetricDecrypt(algo, mode, key, iv, CryptoUtil.hexToBytes(text))
                            }
                            out.toHex()
                        }
                        "RSA" -> {
                            if (action == "encrypt") {
                                CryptoUtil.rsaEncrypt(keyText, text.toByteArray()).toHex()
                            } else {
                                String(CryptoUtil.rsaDecrypt(keyText, CryptoUtil.hexToBytes(text)))
                            }
                        }
                        "SM2" -> {
                            if (action == "encrypt") {
                                CryptoUtil.sm2Encrypt(keyText, text.toByteArray())
                            } else {
                                String(CryptoUtil.sm2Decrypt(keyText, text))
                            }
                        }
                        else -> "未知类型"
                    }
                }.getOrElse { e -> "错误: ${e.message}" }
            },
            "生成密钥对" to {
                if (category == "RSA") {
                    keyPair = CryptoUtil.generateRsaKeyPair()
                } else if (category == "SM2") {
                    keyPair = CryptoUtil.generateSm2KeyPair()
                }
            },
            "清空" to { text = ""; keyText = ""; ivText = ""; result = "" }
        ))

        // 密钥对结果展示
        if (category != "对称" && keyPair.first.isNotEmpty()) {
            SectionCard(title = "密钥对（请复制保存）") {
                Text("公钥:\n${keyPair.first}\n\n私钥:\n${keyPair.second}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }

        ResultBox(title = "结果（Hex）", text = result)
    }
}
