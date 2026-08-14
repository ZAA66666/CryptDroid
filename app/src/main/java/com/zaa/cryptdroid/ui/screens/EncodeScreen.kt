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
import com.zaa.cryptdroid.util.CodecUtil

/**
 * EncodeScreen — 编/解码工具页
 * Base64/Base32/Base58/Hex/URL/Unicode/大小写
 */
@Composable
fun EncodeScreen(onBack: () -> Unit) {
    ToolScaffold(title = "编/解码", onBack = onBack) { _ ->
        var input by remember { mutableStateOf("") }
        var mode by remember { mutableStateOf("encode") } // encode / decode
        var selected by remember { mutableStateOf("Base64") }
        var result by remember { mutableStateOf("") }

        val codecs = listOf("Base64", "Base32", "Base58", "Hex", "URL", "Unicode", "转大写", "转小写", "首字母大写")

        SectionCard(title = "输入") {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("输入文本") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        SectionCard(title = "模式") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = mode == "encode", onClick = { mode = "encode" })
                Text("编码")
                Spacer(Modifier.height(0.dp))
                RadioButton(selected = mode == "decode", onClick = { mode = "decode" })
                Text("解码")
            }
        }

        SectionCard(title = "类型") {
            Column {
                codecs.forEach { label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == label, onClick = { selected = label })
                        Text(label)
                    }
                }
            }
        }

        ActionButtonRow(listOf("执行" to {
            result = runCatching {
                when (selected) {
                    "Base64" -> if (mode == "encode") CodecUtil.base64Encode(input) else CodecUtil.base64Decode(input)
                    "Base32" -> if (mode == "encode") CodecUtil.base32Encode(input) else CodecUtil.base32Decode(input)
                    "Base58" -> if (mode == "encode") CodecUtil.base58Encode(input) else CodecUtil.base58Decode(input)
                    "Hex" -> if (mode == "encode") CodecUtil.hexEncode(input) else CodecUtil.hexDecode(input)
                    "URL" -> if (mode == "encode") CodecUtil.urlEncode(input) else CodecUtil.urlDecode(input)
                    "Unicode" -> if (mode == "encode") CodecUtil.unicodeEncode(input) else CodecUtil.unicodeDecode(input)
                    "转大写" -> CodecUtil.toUpper(input)
                    "转小写" -> CodecUtil.toLower(input)
                    "首字母大写" -> CodecUtil.toTitle(input)
                    else -> "未知类型"
                }
            }.getOrElse { e -> "错误: ${e.message}" }
        }, "清空" to { input = ""; result = "" }))

        ResultBox(title = "结果", text = result)
    }
}
