package com.zaa.cryptdroid.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.applyTo
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * AppIcons — 工具图标集（手写 ImageVector）
 *
 * 为什么不用 material-icons-extended？
 *   - 该库含 2000+ 图标，未开 R8 时整体打包，APK 体积爆炸，违背 ≤6MB 目标。
 *   - 本对象仅用 Material Design 官方 path 数据定义 9 个工具图标，
 *     体积可忽略，且颜色可控（白色，随背景色块变化）。
 *
 * path 数据来源：Material Design Icons (Google) 标准 24x24 路径。
 * 解析方式：Compose 官方 addPathNodes()（不手写解析器，可靠）。
 */
object AppIcons {

    /* 哈希：标签 Tag 图标（# 符号） */
    val Hash: ImageVector by lazy {
        materialIcon(
            name = "Hash",
            pathData = "M21.41,11.58l-9,-9C12.05,2.22 11.55,2 11,2H4c-1.1,0 -2,0.9 -2,2v7c0,0.55 0.22,1.05 0.59,1.42l9,9c0.36,0.36 0.86,0.58 1.41,0.58 0.55,0 1.05,-0.22 1.41,-0.59l7,-7c0.37,-0.36 0.59,-0.86 0.59,-1.41 0,-0.55 -0.23,-1.06 -0.59,-1.42zM5.5,7C4.67,7 4,6.33 4,5.5S4.67,4 5.5,4 7,4.67 7,5.5 6.33,7 5.5,7z"
        )
    }

    /* 编解码：代码 Code 图标 </> */
    val Code: ImageVector by lazy {
        materialIcon(
            name = "Code",
            pathData = "M9.4,16.6L4.8,12l4.6,-4.6L8,6l-6,6 6,6 1.4,-1.4zM14.6,16.6l4.6,-4.6 -4.6,-4.6L16,6l6,6 -6,6 -1.4,-1.4z"
        )
    }

    /* 加解密：锁 Lock */
    val Lock: ImageVector by lazy {
        materialIcon(
            name = "Lock",
            pathData = "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM12,17c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2zM15.1,8H8.9V6c0,-1.71 1.39,-3.1 3.1,-3.1 1.71,0 3.1,1.39 3.1,3.1V8z"
        )
    }

    /* 二维码/条形码：QrCodeScanner */
    val QrCode: ImageVector by lazy {
        materialIcon(
            name = "QrCode",
            pathData = "M3,11h8V3H3V11zM5,5h4v4H5V5zM3,21h8v-8H3V21zM5,15h4v4H5V15zM13,3v8h8V3H13zM19,9h-4V5h4V9zM13,13h2v2h-2V13zM17,13h2v2h-2V13zM13,17h2v2h-2V17zM17,17h2v2h-2V17zM17,21h2v2h-2V21zM13,21h2v2h-2V21z"
        )
    }

    /* JSON：数据对象 DataObject 花括号 */
    val Json: ImageVector by lazy {
        materialIcon(
            name = "Json",
            pathData = "M4,7v2c0,0.55 -0.45,1 -1,1H2v4h1c0.55,0 1,0.45 1,1v2c0,1.65 1.35,3 3,3h3v-2H7c-0.55,0 -1,-0.45 -1,-1v-2c0,-1.3 -0.84,-2.42 -2,-2.83v-0.34C5.16,11.42 6,10.3 6,9V7c0,-0.55 0.45,-1 1,-1h3V4H7C5.35,4 4,5.35 4,7zM17,4h-3v2h3c0.55,0 1,0.45 1,1v2c0,1.3 0.84,2.42 2,2.83v0.34c-1.16,0.41 -2,1.52 -2,2.83v2c0,0.55 -0.45,1 -1,1h-3v2h3c1.65,0 3,-1.35 3,-3v-2c0,-0.55 0.45,-1 1,-1h1v-4h-1c-0.55,0 -1,-0.45 -1,-1V7c0,-1.65 -1.35,-3 -3,-3z"
        )
    }

    /* 随机文本：骰子 Casino（随机感） */
    val Dice: ImageVector by lazy {
        materialIcon(
            name = "Dice",
            pathData = "M19,3H5c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5c0,-1.1 -0.9,-2 -2,-2zM7.5,18c-0.83,0 -1.5,-0.67 -1.5,-1.5S6.67,15 7.5,15s1.5,0.67 1.5,1.5S8.33,18 7.5,18zM7.5,9C6.67,9 6,8.33 6,7.5S6.67,6 7.5,6 9,6.67 9,7.5 8.33,9 7.5,9zM12,13.5c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5zM16.5,18c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5zM16.5,9c-0.83,0 -1.5,-0.67 -1.5,-1.5S15.67,6 16.5,6s1.5,0.67 1.5,1.5S17.33,9 16.5,9z"
        )
    }

    /* 文本工具：TextFields */
    val TextFields: ImageVector by lazy {
        materialIcon(
            name = "TextFields",
            pathData = "M2.5,4v3h5v12h3V7h5V4H2.5zM21.5,9h-9v3h3v7h3v-7h3V9z"
        )
    }

    /* Crontab：Schedule 时钟 */
    val Schedule: ImageVector by lazy {
        materialIcon(
            name = "Schedule",
            pathData = "M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z"
        )
    }

    /* 使用教程：MenuBook */
    val MenuBook: ImageVector by lazy {
        materialIcon(
            name = "MenuBook",
            pathData = "M21,5c-1.11,-0.35 -2.33,-0.5 -3.5,-0.5 -1.95,0 -4.05,0.4 -5.5,1.5 -1.45,-1.1 -3.55,-1.5 -5.5,-1.5 -1.17,0 -2.39,0.15 -3.5,0.5 -0.61,0.2 -1,0.76 -1,1.4v13.65c0,0.55 0.45,1 1,1 0.1,0 0.15,-0.05 0.25,-0.05C4.1,20.45 5.05,20 6.5,20c1.95,0 4.05,0.4 5.5,1.5 1.35,-0.85 3.8,-1.5 5.5,-1.5 1.65,0 3.35,0.3 4.75,1.05 0.1,0.05 0.15,0.05 0.25,0.05 0.55,0 1,-0.45 1,-1V6.4c0,-0.64 -0.39,-1.2 -1,-1.4zM16,19.5c-1.7,0 -4.15,0.65 -5.5,1.5V8c1.35,-0.85 3.8,-1.5 5.5,-1.5 1.2,0 2.4,0.15 3.5,0.5v11.5c-1.1,-0.35 -2.3,-0.5 -3.5,-0.5z"
        )
    }

    /** 从 Material 官方 path 数据构建 24x24 ImageVector（Compose addPathNodes 解析） */
    private fun materialIcon(name: String, pathData: String): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // addPathNodes 解析 SVG path → PathNode 列表
                // applyTo 扩展把每个节点应用到当前 PathBuilder
                addPathNodes(pathData).forEach { node ->
                    node.applyTo(this)
                }
            }
        }.build()
    }
}
