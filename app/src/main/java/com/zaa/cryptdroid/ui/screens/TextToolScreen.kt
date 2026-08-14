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
import com.zaa.cryptdroid.util.TextUtil

/**
 * TextToolScreen — 文本工具：统计 / 去重 / 排序 / 对比
 */
@Composable
fun TextToolScreen(onBack: () -> Unit) {
    ToolScaffold(title = "文本工具", onBack = onBack) { _ ->
        var input by remember { mutableStateOf("") }
        var compareText by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }

        SectionCard(title = "输入文本") {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("文本内容") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
        }

        ActionButtonRow(listOf(
            "统计" to {
                val (chars, words, lines) = TextUtil.stats(input)
                result = "字符数(不含空白): $chars\n单词数: $words\n行数: $lines"
            },
            "去重" to { result = TextUtil.removeDuplicateLines(input) },
            "排序" to { result = TextUtil.sortLines(input) },
            "清空" to { input = ""; result = "" }
        ))

        ResultBox(title = "结果", text = result)

        // 文本对比
        SectionCard(title = "文本对比") {
            Text("对比下方文本与上方文本的差异", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = compareText,
                onValueChange = { compareText = it },
                label = { Text("要对比的文本") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
        }

        ActionButtonRow(listOf(
            "对比" to { result = TextUtil.diffLines(input, compareText) }
        ))

        ResultBox(title = "差异结果", text = result)
    }
}
