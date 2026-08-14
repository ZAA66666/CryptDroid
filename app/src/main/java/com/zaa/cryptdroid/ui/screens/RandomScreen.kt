package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.zaa.cryptdroid.ui.components.PwaButton
import com.zaa.cryptdroid.ui.components.PwaFieldLabel
import com.zaa.cryptdroid.ui.components.PwaResultBox
import com.zaa.cryptdroid.ui.components.PwaScreen
import com.zaa.cryptdroid.ui.components.PwaTextField
import com.zaa.cryptdroid.util.RandomUtil

/**
 * RandomScreen — 随机文本/虚假数据（原版风格）
 */
@Composable
fun RandomScreen(onBack: () -> Unit) {
    PwaScreen(title = "随机文本", onBack = onBack) {
        var length by remember { mutableIntStateOf(16) }
        var useUpper by remember { mutableStateOf(true) }
        var useDigits by remember { mutableStateOf(true) }
        var useSymbols by remember { mutableStateOf(false) }
        var result by remember { mutableStateOf("") }

        PwaFieldLabel("随机密码")
        PwaTextField(length.toString(), { length = it.toIntOrNull() ?: 16 }, "长度")

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = useUpper, onCheckedChange = { useUpper = it })
            Text("大写字母", color = Color(0xFF1A1A1A))
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.padding(8.dp))
            Checkbox(checked = useDigits, onCheckedChange = { useDigits = it })
            Text("数字", color = Color(0xFF1A1A1A))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = useSymbols, onCheckedChange = { useSymbols = it })
            Text("符号", color = Color(0xFF1A1A1A))
        }

        PwaButton("生成密码") { result = RandomUtil.password(length, useUpper, useDigits, useSymbols) }
        PwaButton("随机数字") { result = RandomUtil.digits(length) }
        PwaButton("UUID") { result = RandomUtil.uuid() }

        PwaResultBox("结果", result)

        PwaFieldLabel("虚假数据")
        PwaButton("姓名") { result = RandomUtil.name() }
        PwaButton("手机号") { result = RandomUtil.phone() }
        PwaButton("邮箱") { result = RandomUtil.email() }
        PwaButton("城市") { result = RandomUtil.city() }
        PwaButton("日期") { result = RandomUtil.date() }
        PwaButton("IP") { result = RandomUtil.ip() }
        PwaButton("时间戳") { result = RandomUtil.timestamp() }

        PwaResultBox("虚假数据结果", result)
    }
}
