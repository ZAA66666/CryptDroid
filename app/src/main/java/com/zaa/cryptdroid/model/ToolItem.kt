package com.zaa.cryptdroid.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.zaa.cryptdroid.R
import com.zaa.cryptdroid.ui.Screen

/**
 * 工具条目模型（Compose 版）
 * 每个工具 = 图标 + 强调色 + 标题 + 描述 + 对应页面。
 *
 * 图标说明：仅用 material-icons-core（material3 自带），不引 icons-extended 防体积爆炸。
 */
data class ToolItem(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val color: Color,
    val screen: Screen
) {
    companion object {
        /** 9 个工具清单（顺序与 PWA 版主页一致） */
        fun all(): List<ToolItem> = listOf(
            ToolItem("hash", R.string.tool_hash, R.string.tool_hash_desc,
                Icons.Filled.Build, Color(0xFF00A862), Screen.HASH),
            ToolItem("encode", R.string.tool_encode, R.string.tool_encode_desc,
                Icons.Filled.Create, Color(0xFF2196F3), Screen.ENCODE),
            ToolItem("cipher", R.string.tool_cipher, R.string.tool_cipher_desc,
                Icons.Filled.Lock, Color(0xFF9C27B0), Screen.CIPHER),
            ToolItem("qr", R.string.tool_qr, R.string.tool_qr_desc,
                Icons.Filled.List, Color(0xFFFF9800), Screen.QR),
            ToolItem("json", R.string.tool_json, R.string.tool_json_desc,
                Icons.Filled.Info, Color(0xFF4CAF50), Screen.JSON),
            ToolItem("random", R.string.tool_random, R.string.tool_random_desc,
                Icons.Filled.Refresh, Color(0xFF795548), Screen.RANDOM),
            ToolItem("text", R.string.tool_text, R.string.tool_text_desc,
                Icons.Filled.Edit, Color(0xFF607D8B), Screen.TEXT),
            ToolItem("crontab", R.string.tool_crontab, R.string.tool_crontab_desc,
                Icons.Filled.DateRange, Color(0xFFE91E63), Screen.CRONTAB),
            ToolItem("guide", R.string.tool_guide, R.string.tool_guide_desc,
                Icons.Filled.Star, Color(0xFF3F51B5), Screen.GUIDE)
        )
    }
}
