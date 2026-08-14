package com.zaa.cryptdroid.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * ToolScaffold — 工具页通用外壳
 * 提供：返回按钮 + 标题 + 可滚动内容区
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            content(Modifier)
        }
    }
}

/**
 * SectionCard — 卡片分组容器（输入区 / 结果区）
 */
@Composable
fun SectionCard(
    title: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(12.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
            }
            content()
        }
    }
}

/**
 * ResultBox — 结果展示区（可选中复制，带复制按钮）
 */
@Composable
fun ResultBox(
    title: String,
    text: String,
    placeholder: String = "结果将显示在这里"
) {
    SectionCard(title = title) {
        SelectionContainer {
            Text(
                text = text.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyMedium,
                color = if (text.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        }
        if (text.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            val clipboard = LocalClipboardManager.current
            OutlinedButton(
                onClick = { clipboard.setText(AnnotatedString(text)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("复制结果") }
        }
    }
}

/**
 * CopyableTextField — 带复制按钮的只读文本（用于密钥/PEM 等长文本）
 */
@Composable
fun CopyableTextField(label: String, text: String, onCopy: (String) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { onCopy(text) }) {
                Text("复制", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

/** 通用操作按钮行 */
@Composable
fun ActionButtonRow(actions: List<Pair<String, () -> Unit>>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        actions.forEachIndexed { index, (label, onClick) ->
            if (index > 0) Spacer(Modifier.width(8.dp))
            Button(onClick = onClick, modifier = Modifier.weight(1f)) {
                Text(label)
            }
        }
    }
}
