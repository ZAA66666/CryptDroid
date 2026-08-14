package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.zaa.cryptdroid.util.CodecUtil

/**
 * EncodeScreen — 编/解码工具页（原版风格）
 */
@Composable
fun EncodeScreen(onBack: () -> Unit) {
    PwaScreen(title = "编/解码", onBack = onBack) {
        var input by remember { mutableStateOf("") }
        var mode by remember { mutableStateOf("encode") }
        var selected by remember { mutableStateOf("Base64") }
        var result by remember { mutableStateOf("") }

        val codecs = listOf("Base64", "Base32", "Base58", "Hex", "URL", "Unicode", "转大写", "转小写", "首字母大写")

        PwaFieldLabel("输入文本")
        PwaTextField(input, { input = it }, "输入文本", minLines = 3)

        PwaFieldLabel("模式")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = mode == "encode", onClick = { mode = "encode" })
            Text("编码", color = Color(0xFF1A1A1A))
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            RadioButton(selected = mode == "decode", onClick = { mode = "decode" })
            Text("解码", color = Color(0xFF1A1A1A))
        }

        PwaFieldLabel("类型")
        Column {
            codecs.forEach { label ->
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

        PwaButton("执行") {
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
        }

        PwaResultBox("结果", result)
    }
}
