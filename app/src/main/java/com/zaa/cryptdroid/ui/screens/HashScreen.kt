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
import com.zaa.cryptdroid.util.HashUtil

/**
 * HashScreen — 哈希工具页
 * 普通哈希（MD5/SHA 系列/SHA3）+ HMAC 系列
 */
@Composable
fun HashScreen(onBack: () -> Unit) {
    ToolScaffold(title = "哈希 Hash", onBack = onBack) { _ ->
        var input by remember { mutableStateOf("") }
        var key by remember { mutableStateOf("") }
        var isHmac by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf("MD5") }
        var result by remember { mutableStateOf("") }

        SectionCard(title = "输入") {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("要哈希的文本") },
                modifier = Modifier.fillMaxWidth()
            )
            if (isHmac) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("HMAC 密钥") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SectionCard(title = "类型") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !isHmac, onClick = { isHmac = false })
                Text("普通哈希")
                Spacer(Modifier.height(0.dp))
                RadioButton(selected = isHmac, onClick = { isHmac = true })
                Text("HMAC")
            }
        }

        SectionCard(title = "算法") {
            Column {
                val list = if (isHmac) HashUtil.HMAC_ALGORITHMS.map { it.first } else HashUtil.ALGORITHMS.map { it.first }
                list.forEach { label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == label, onClick = { selected = label })
                        Text(label)
                    }
                }
            }
        }

        ActionButtonRow(listOf("计算" to {
            result = runCatching {
                if (isHmac) {
                    val algo = HashUtil.HMAC_ALGORITHMS.first { it.first == selected }.second
                    HashUtil.hmac(input, key, algo)
                } else {
                    HashUtil.hash(input, selected)
                }
            }.getOrElse { e -> "错误: ${e.message}" }
        }, "清空" to { input = ""; key = ""; result = "" }))

        ResultBox(title = "结果", text = result)
    }
}
