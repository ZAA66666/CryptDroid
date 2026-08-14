package com.zaa.cryptdroid.util

/**
 * TextUtil — 文本统计 / 去重 / 排序 / 对比
 */
object TextUtil {

    /** 统计：字符数（不含空白）、单词数、行数 */
    fun stats(text: String): Triple<Int, Int, Int> {
        val chars = text.filterNot { it.isWhitespace() }.length
        val words = text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
        val lines = text.split("\n").count { it.isNotEmpty() }
        return Triple(chars, words, lines)
    }

    /** 去除重复行，返回去重后文本 */
    fun removeDuplicateLines(text: String): String {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<String>()
        text.split("\n").forEach { line ->
            val key = line.trim()
            if (key.isNotEmpty() && seen.add(key)) {
                result.add(line)
            }
        }
        return result.joinToString("\n")
    }

    /** 按行排序（升序） */
    fun sortLines(text: String): String =
        text.split("\n").filter { it.isNotBlank() }.sorted().joinToString("\n")

    /** 行差对比（简单 LCS 风格），返回带 +/- 前缀的行 */
    fun diffLines(oldText: String, newText: String): String {
        val oldLines = oldText.split("\n")
        val newLines = newText.split("\n")
        val sb = StringBuilder()
        val max = maxOf(oldLines.size, newLines.size)
        for (i in 0 until max) {
            val old = oldLines.getOrNull(i) ?: ""
            val new = newLines.getOrNull(i) ?: ""
            when {
                old == new -> sb.append("  ").append(old).append("\n")
                old.isEmpty() -> sb.append("+ ").append(new).append("\n")
                new.isEmpty() -> sb.append("- ").append(old).append("\n")
                else -> {
                    sb.append("- ").append(old).append("\n")
                    sb.append("+ ").append(new).append("\n")
                }
            }
        }
        return sb.toString().trimEnd()
    }
}
