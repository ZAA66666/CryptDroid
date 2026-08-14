package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.zaa.cryptdroid.ui.components.PwaResultBox
import com.zaa.cryptdroid.ui.components.PwaScreen
import com.zaa.cryptdroid.ui.components.PwaTextField
import com.zaa.cryptdroid.util.HashUtil

/**
 * HashScreen — 哈希工具页（原版风格）
 * 普通哈希 + HMAC
 */
@Composable
fun HashScreen(onBack: () -> Unit) {
    PwaScreen(title = "哈希", onBack = onBack) {
        var input by remember { mutableStateOf("") }
        var key by remember { mutableStateOf("") }
        var isHmac by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf("SHA-256") }
        var result by remember { mutableStateOf("") }

        PwaFieldLabel("输入文本")
        PwaTextField(input, { input = it }, "要计算哈希的文本，如 hello", minLines = 2)

        PwaFieldLabel("类型")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !isHmac, onClick = { isHmac = false })
            Text("普通哈希", color = Color(0xFF1A1A1A))
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = isHmac, onClick = { isHmac = true })
            Text("HMAC", color = Color(0xFF1A1A1A))
        }

        PwaFieldLabel("算法")
        val list = if (isHmac) HashUtil.HMAC_ALGORITHMS.map { it.first } else HashUtil.ALGORITHMS.map { it.first }
        Column {
            list.forEach { label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = label }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == label, onClick = { selected = label })
                    Text(label, color = Color(0xFF1A1A1A))
                }
            }
        }

        if (isHmac) {
            PwaFieldLabel("密钥（HMAC 需要）")
            PwaTextField(key, { key = it }, "HMAC 密钥")
        }

        PwaButton("计算哈希") {
            result = runCatching {
                if (isHmac) {
                    val algo = HashUtil.HMAC_ALGORITHMS.first { it.first == selected }.second
                    HashUtil.hmac(input, key, algo)
                } else {
                    HashUtil.hash(input, selected)
                }
            }.getOrElse { e -> "错误: ${e.message}" }
        }

        PwaResultBox("结果（十六进制）", result)
    }
}
