package com.zaa.cryptdroid.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaa.cryptdroid.ui.components.SectionCard
import com.zaa.cryptdroid.ui.components.ToolScaffold

/**
 * GuideScreen — 使用教程页（静态说明卡片）
 */
@Composable
fun GuideScreen(onBack: () -> Unit) {
    ToolScaffold(title = "使用教程", onBack = onBack) { _ ->
        SectionCard(title = "哈希 Hash") {
            Text("对文本计算 MD5 / SHA 系列 / SHA3 / HMAC 摘要。用于校验文件完整性、验证数据一致性。HMAC 需额外输入密钥。")
        }
        SectionCard(title = "编/解码") {
            Text("Base64 / Base32 / Base58 / Hex / URL / Unicode 的互相转换。Base58 常用于加密货币地址，Base64 常用于数据传输。")
        }
        SectionCard(title = "加/解密") {
            Text("对称加密（AES/DES/3DES/Blowfish/RC4/Rabbit）：用同一个密钥加解密。RSA：非对称，公钥加密私钥解密。SM2：国密非对称。加密结果以 Hex 显示。")
        }
        SectionCard(title = "二维码") {
            Text("输入文本或链接，一键生成二维码图片，可截图分享或扫码。")
        }
        SectionCard(title = "JSON") {
            Text("格式化（美化缩进）、压缩（去掉空白）、校验合法性、提取所有键路径，方便查看嵌套结构。")
        }
        SectionCard(title = "随机文本") {
            Text("生成随机密码（可配大小写/数字/符号）、随机数字、UUID，以及姓名/手机号/邮箱/地址等虚假数据。")
        }
        SectionCard(title = "文本工具") {
            Text("统计字符/单词/行数，按行去重与排序，对比两段文本差异。")
        }
        SectionCard(title = "Crontab") {
            Text("解析 5 段 Cron 表达式（分 时 日 月 周），计算下次执行时间。示例：*/5 * * * * = 每5分钟。")
        }
        SectionCard(title = "安全提示") {
            Text("本应用全部功能离线运行，数据不上传。设置主密码后，密码本数据以 AES-256-GCM 加密存储，主密码永不上传。")
        }
    }
}
