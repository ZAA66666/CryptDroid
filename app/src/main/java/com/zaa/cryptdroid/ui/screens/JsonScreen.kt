package com.zaa.cryptdroid.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zaa.cryptdroid.ui.components.PwaButton
import com.zaa.cryptdroid.ui.components.PwaFieldLabel
import com.zaa.cryptdroid.ui.components.PwaResultBox
import com.zaa.cryptdroid.ui.components.PwaScreen
import com.zaa.cryptdroid.ui.components.PwaTextField
import com.zaa.cryptdroid.util.JsonUtil

/**
 * JsonScreen — JSON 格式化/压缩/校验（原版风格）
 */
@Composable
fun JsonScreen(onBack: () -> Unit) {
    PwaScreen(title = "JSON", onBack = onBack) {
        var input by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }
        var keys by remember { mutableStateOf("") }

        PwaFieldLabel("输入 JSON")
        PwaTextField(input, { input = it }, "粘贴 JSON 文本", minLines = 5)

        PwaButton("格式化") {
            result = runCatching { JsonUtil.format(input) }.getOrElse { e -> "错误: ${e.message}" }
        }
        PwaButton("压缩") {
            result = runCatching { JsonUtil.compress(input) }.getOrElse { e -> "错误: ${e.message}" }
        }
        PwaButton("校验") {
            result = if (JsonUtil.isValid(input)) "✅ 合法 JSON" else "❌ 不是合法 JSON"
        }
        PwaButton("提取键路径") {
            keys = runCatching { JsonUtil.listKeys(input).joinToString("\n") }.getOrElse { e -> "错误: ${e.message}" }
        }

        PwaResultBox("结果", result)
        if (keys.isNotEmpty()) {
            PwaResultBox("键路径", keys)
        }
    }
}
