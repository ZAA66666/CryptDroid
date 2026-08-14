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
import androidx.compose.ui.unit.dp
import com.zaa.cryptdroid.ui.components.ActionButtonRow
import com.zaa.cryptdroid.ui.components.ResultBox
import com.zaa.cryptdroid.ui.components.SectionCard
import com.zaa.cryptdroid.ui.components.ToolScaffold
import com.zaa.cryptdroid.util.JsonUtil

/**
 * JsonScreen — JSON 格式化 / 校验 / 键值提取
 */
@Composable
fun JsonScreen(onBack: () -> Unit) {
    ToolScaffold(title = "JSON", onBack = onBack) { _ ->
        var input by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }
        var keys by remember { mutableStateOf("") }

        SectionCard(title = "输入 JSON") {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("粘贴 JSON 文本") },
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        ActionButtonRow(listOf(
            "格式化" to {
                result = runCatching { JsonUtil.format(input) }.getOrElse { e -> "错误: ${e.message}" }
            },
            "压缩" to {
                result = runCatching { JsonUtil.compress(input) }.getOrElse { e -> "错误: ${e.message}" }
            },
            "校验" to {
                result = if (JsonUtil.isValid(input)) "✅ 合法 JSON" else "❌ 不是合法 JSON"
            },
            "提取键" to {
                keys = runCatching {
                    JsonUtil.listKeys(input).joinToString("\n")
                }.getOrElse { e -> "错误: ${e.message}" }
            }
        ))

        ResultBox(title = "结果", text = result)

        if (keys.isNotEmpty()) {
            ResultBox(title = "键路径", text = keys)
        }
    }
}
