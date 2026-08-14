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
import com.zaa.cryptdroid.util.CronParser

/**
 * CrontabScreen — Cron 表达式解析
 */
@Composable
fun CrontabScreen(onBack: () -> Unit) {
    ToolScaffold(title = "Crontab", onBack = onBack) { _ ->
        var expr by remember { mutableStateOf("*/5 * * * *") }
        var result by remember { mutableStateOf("") }

        SectionCard(title = "表达式") {
            OutlinedTextField(
                value = expr,
                onValueChange = { expr = it },
                label = { Text("格式：分 时 日 月 周") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text("示例：*/5 * * * * （每5分钟）", style = MaterialTheme.typography.bodySmall)
            Text("示例：0 9 * * 1-5 （工作日9点）", style = MaterialTheme.typography.bodySmall)
        }

        ActionButtonRow(listOf(
            "解析" to {
                result = runCatching {
                    val list = CronParser.nextExecutions(expr)
                    "下次执行时间（5 个）：\n" + list.joinToString("\n") { it.toString().replace("T", " ") }
                }.getOrElse { e -> "错误: ${e.message}" }
            }
        ))

        ResultBox(title = "结果", text = result)
    }
}
