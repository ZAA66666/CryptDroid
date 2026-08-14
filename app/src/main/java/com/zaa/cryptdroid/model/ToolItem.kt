package com.zaa.cryptdroid.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.zaa.cryptdroid.R
import com.zaa.cryptdroid.ui.theme.AppIcons

/**
 * 工具条目模型（Compose 版）
 * 对应 DEV_DOC_JAVA.md 主页功能列表的 9 个工具。
 * 每个工具 = 图标 + 强调色 + 标题 + 描述。
 */
data class ToolItem(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val color: Color
) {
    companion object {
        /** 9 个工具清单（顺序与 PWA 版主页一致） */
        fun all(): List<ToolItem> = listOf(
            ToolItem("hash", R.string.tool_hash, R.string.tool_hash_desc,
                AppIcons.Hash, Color(0xFF00A862)),
            ToolItem("encode", R.string.tool_encode, R.string.tool_encode_desc,
                AppIcons.Code, Color(0xFF2196F3)),
            ToolItem("cipher", R.string.tool_cipher, R.string.tool_cipher_desc,
                AppIcons.Lock, Color(0xFF9C27B0)),
            ToolItem("qr", R.string.tool_qr, R.string.tool_qr_desc,
                AppIcons.QrCode, Color(0xFFFF9800)),
            ToolItem("json", R.string.tool_json, R.string.tool_json_desc,
                AppIcons.Json, Color(0xFF4CAF50)),
            ToolItem("random", R.string.tool_random, R.string.tool_random_desc,
                AppIcons.Dice, Color(0xFF795548)),
            ToolItem("text", R.string.tool_text, R.string.tool_text_desc,
                AppIcons.TextFields, Color(0xFF607D8B)),
            ToolItem("crontab", R.string.tool_crontab, R.string.tool_crontab_desc,
                AppIcons.Schedule, Color(0xFFE91E63)),
            ToolItem("guide", R.string.tool_guide, R.string.tool_guide_desc,
                AppIcons.MenuBook, Color(0xFF3F51B5))
        )
    }
}
