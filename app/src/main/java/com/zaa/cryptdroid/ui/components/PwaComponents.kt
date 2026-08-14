package com.zaa.cryptdroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PwaComponents — 原 PWA 版视觉风格的 Compose 组件
 * 严格对齐 crypto-pwa 的 css/style.css 参数：
 *  - 主按钮：全宽、绿底、圆角12、投影
 *  - 次按钮：浅绿底(#e6f6ee)、绿字
 *  - 输入框：浅灰底(#fafbfc)、圆角12、聚焦绿边+光环
 *  - 字段标签：12.5px 灰、字重600
 *  - 面板头：sticky、返回箭头(绿26px) + 居中标题(17px bold)
 */

/* 原版色板（与 crypto-pwa :root 一致） */
private val Accent = Color(0xFF00A862)
private val AccentSoft = Color(0xFFE6F6EE)
private val AccentRing = Color(0x2E00A862)  // rgba(0,168,98,0.18)
private val PageBg = Color(0xFFF5F6F8)
private val CardBg = Color(0xFFFFFFFF)
private val FieldBg = Color(0xFFFAFBFC)
private val TextMain = Color(0xFF1A1A1A)
private val TextMuted = Color(0xFF8A8F96)
private val Border = Color(0xFFE3E5E8)

/**
 * PwaScreen — 原版风格页面外壳：sticky 面板头（返回 + 居中标题）+ 可滚动内容
 */
@Composable
fun PwaScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
    ) {
        // sticky 面板头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = Accent, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = title,
                color = TextMain,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.width(38.dp)) // 平衡返回按钮宽度
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            content()
        }
    }
}

/**
 * PwaFieldLabel — 原版字段标签
 */
@Composable
fun PwaFieldLabel(text: String) {
    Text(
        text = text,
        color = TextMuted,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 7.dp)
    )
}

/**
 * PwaTextField — 原版风格输入框（浅灰底、圆角12、聚焦绿边）
 */
@Composable
fun PwaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted.copy(alpha = 0.7f), fontSize = 15.sp) },
        minLines = minLines,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = FieldBg,
            focusedBorderColor = Accent,
            unfocusedBorderColor = Border,
            cursorColor = Accent
        )
    )
}

/**
 * PwaButton — 原版主按钮（全宽、绿底、圆角12）
 */
@Composable
fun PwaButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * PwaGhostButton — 原版次按钮（浅绿底、绿字）
 */
@Composable
fun PwaGhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentSoft,
            contentColor = Accent
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * PwaResultBox — 原版结果区（只读输入框样式 + 复制按钮）
 */
@Composable
fun PwaResultBox(
    title: String,
    text: String,
    placeholder: String = "结果将显示在这里"
) {
    PwaFieldLabel(title)
    val clipboard = LocalClipboardManager.current
    OutlinedTextField(
        value = text,
        onValueChange = {},
        readOnly = true,
        placeholder = { Text(placeholder, color = TextMuted.copy(alpha = 0.7f), fontSize = 15.sp) },
        minLines = 4,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FieldBg,
            unfocusedContainerColor = FieldBg,
            focusedBorderColor = Accent,
            unfocusedBorderColor = Border,
            cursorColor = Accent,
            disabledContainerColor = FieldBg,
            disabledBorderColor = Border,
            disabledTextColor = TextMain
        )
    )
    if (text.isNotEmpty()) {
        PwaGhostButton("复制结果") { clipboard.setText(AnnotatedString(text)) }
    }
}
