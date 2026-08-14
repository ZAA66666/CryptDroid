package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.zaa.cryptdroid.ui.components.PwaButton
import com.zaa.cryptdroid.ui.components.PwaFieldLabel
import com.zaa.cryptdroid.ui.components.PwaScreen
import com.zaa.cryptdroid.ui.components.PwaTextField
import com.zaa.cryptdroid.util.QrUtil

/**
 * QrScreen — 二维码生成页（原版风格）
 */
@Composable
fun QrScreen(onBack: () -> Unit) {
    PwaScreen(title = "二维码", onBack = onBack) {
        var content by remember { mutableStateOf("") }
        var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        var error by remember { mutableStateOf("") }

        PwaFieldLabel("内容")
        PwaTextField(content, { content = it }, "二维码内容（文本/链接等）", minLines = 2)

        PwaButton("生成二维码") {
            error = ""
            qrBitmap = runCatching { QrUtil.generateQr(content) }.getOrElse { e ->
                error = "生成失败: ${e.message}"
                null
            }
        }

        if (error.isNotEmpty()) {
            androidx.compose.material3.Text(error, color = Color(0xFFE53935))
        }

        if (qrBitmap != null) {
            PwaFieldLabel("预览")
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "二维码",
                    modifier = Modifier.size(240.dp)
                )
            }
        }
    }
}
