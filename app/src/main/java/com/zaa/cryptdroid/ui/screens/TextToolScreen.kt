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
import com.zaa.cryptdroid.util.TextUtil

/**
 * TextToolScreen — 文本工具（原版风格）
 */
@Composable
fun TextToolScreen(onBack: () -> Unit) {
    PwaScreen(title = "文本工具", onBack = onBack) {
        var input by remember { mutableStateOf("") }
        var compareText by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }

        PwaFieldLabel("输入文本")
        PwaTextField(input, { input = it }, "文本内容", minLines = 4)

        PwaButton("统计") {
            val (chars, words, lines) = TextUtil.stats(input)
            result = "字符数(不含空白): $chars\n单词数: $words\n行数: $lines"
        }
        PwaButton("去重") { result = TextUtil.removeDuplicateLines(input) }
        PwaButton("排序") { result = TextUtil.sortLines(input) }

        PwaResultBox("结果", result)

        PwaFieldLabel("文本对比")
        PwaTextField(compareText, { compareText = it }, "要对比的文本", minLines = 4)
        PwaButton("对比") { result = TextUtil.diffLines(input, compareText) }

        PwaResultBox("差异结果", result)
    }
}
