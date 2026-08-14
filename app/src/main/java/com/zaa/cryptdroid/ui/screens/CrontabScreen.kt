package com.zaa.cryptdroid.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.zaa.cryptdroid.ui.components.PwaButton
import com.zaa.cryptdroid.ui.components.PwaFieldLabel
import com.zaa.cryptdroid.ui.components.PwaResultBox
import com.zaa.cryptdroid.ui.components.PwaScreen
import com.zaa.cryptdroid.ui.components.PwaTextField
import com.zaa.cryptdroid.util.CronParser

/**
 * CrontabScreen — Cron 表达式解析（原版风格）
 */
@Composable
fun CrontabScreen(onBack: () -> Unit) {
    PwaScreen(title = "Crontab", onBack = onBack) {
        var expr by remember { mutableStateOf("*/5 * * * *") }
        var result by remember { mutableStateOf("") }

        PwaFieldLabel("表达式")
        PwaTextField(expr, { expr = it }, "格式：分 时 日 月 周", minLines = 2)

        Text("示例：*/5 * * * *（每5分钟）", color = Color(0xFF8A8F96))
        Text("示例：0 9 * * 1-5（工作日9点）", color = Color(0xFF8A8F96))

        PwaButton("解析") {
            result = runCatching {
                val list = CronParser.nextExecutions(expr)
                "下次执行时间（5 个）：\n" + list.joinToString("\n") { it.toString().replace("T", " ") }
            }.getOrElse { e -> "错误: ${e.message}" }
        }

        PwaResultBox("结果", result)
    }
}
