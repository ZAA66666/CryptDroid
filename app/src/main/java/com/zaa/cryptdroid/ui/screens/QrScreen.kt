package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.zaa.cryptdroid.ui.components.ActionButtonRow
import com.zaa.cryptdroid.ui.components.SectionCard
import com.zaa.cryptdroid.ui.components.ToolScaffold
import com.zaa.cryptdroid.util.QrUtil

/**
 * QrScreen — 二维码生成页
 */
@Composable
fun QrScreen(onBack: () -> Unit) {
    ToolScaffold(title = "二维码", onBack = onBack) { _ ->
        var content by remember { mutableStateOf("") }
        var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        var error by remember { mutableStateOf("") }

        SectionCard(title = "内容") {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("二维码内容（文本/链接等）") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        ActionButtonRow(listOf(
            "生成" to {
                error = ""
                qrBitmap = runCatching { QrUtil.generateQr(content) }.getOrElse { e ->
                    error = "生成失败: ${e.message}"
                    null
                }
            },
            "清空" to { content = ""; qrBitmap = null; error = "" }
        ))

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        if (qrBitmap != null) {
            SectionCard(title = "预览") {
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
}
