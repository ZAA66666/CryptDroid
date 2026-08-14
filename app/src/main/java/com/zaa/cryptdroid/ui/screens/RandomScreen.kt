package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.zaa.cryptdroid.util.RandomUtil

/**
 * RandomScreen — 随机文本 / 密码生成 / 虚假数据
 */
@Composable
fun RandomScreen(onBack: () -> Unit) {
    ToolScaffold(title = "随机文本", onBack = onBack) { _ ->
        var length by remember { mutableIntStateOf(16) }
        var useUpper by remember { mutableStateOf(true) }
        var useDigits by remember { mutableStateOf(true) }
        var useSymbols by remember { mutableStateOf(false) }
        var result by remember { mutableStateOf("") }

        SectionCard(title = "随机密码") {
            OutlinedTextField(
                value = length.toString(),
                onValueChange = { length = it.toIntOrNull() ?: 16 },
                label = { Text("长度") },
                modifier = Modifier.fillMaxWidth()
            )
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useUpper, onCheckedChange = { useUpper = it })
                    Text("大写字母")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useDigits, onCheckedChange = { useDigits = it })
                    Text("数字")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useSymbols, onCheckedChange = { useSymbols = it })
                    Text("符号")
                }
            }
        }

        ActionButtonRow(listOf(
            "生成密码" to { result = RandomUtil.password(length, useUpper, useDigits, useSymbols) },
            "随机数字" to { result = RandomUtil.digits(length) },
            "UUID" to { result = RandomUtil.uuid() }
        ))

        ResultBox(title = "结果", text = result)

        // 虚假数据
        SectionCard(title = "虚假数据") {
            Text("姓名/手机号/邮箱/城市/日期/IP 一键生成", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }

        ActionButtonRow(listOf(
            "姓名" to { result = RandomUtil.name() },
            "手机号" to { result = RandomUtil.phone() },
            "邮箱" to { result = RandomUtil.email() },
            "城市" to { result = RandomUtil.city() }
        ))
        ActionButtonRow(listOf(
            "日期" to { result = RandomUtil.date() },
            "IP" to { result = RandomUtil.ip() },
            "时间戳" to { result = RandomUtil.timestamp() }
        ))

        ResultBox(title = "虚假数据结果", text = result)
    }
}
