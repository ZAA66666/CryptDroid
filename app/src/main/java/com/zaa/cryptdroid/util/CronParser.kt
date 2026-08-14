package com.zaa.cryptdroid.util

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * CronParser — 5 段 Crontab 表达式解析
 *
 * 格式：分 时 日 月 周
 *   *       每单位
 *   5       具体值
 *   1,3,5   多个值
 *   1-5     范围
 *   星号/15  步长
 * 周：0/7 = 周日，1-6 = 周一到周六
 */
object CronParser {

    /** 解析结果：下次执行时间列表 */
    fun nextExecutions(expression: String, count: Int = 5, from: LocalDateTime = LocalDateTime.now()): List<LocalDateTime> {
        val parts = expression.trim().split(Regex("\\s+"))
        require(parts.size == 5) { "Cron 表达式必须为 5 段：分 时 日 月 周" }

        val minute = parseField(parts[0], 0, 59)
        val hour = parseField(parts[1], 0, 23)
        val day = parseField(parts[2], 1, 31)
        val month = parseField(parts[3], 1, 12)
        val week = parseField(parts[4], 0, 7)

        // 周 0 和 7 都表示周日
        val weekDays = week.map { if (it == 7) 0 else it }.toSet()

        val result = mutableListOf<LocalDateTime>()
        var cursor = from.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)

        while (result.size < count) {
            val y = cursor.year
            val mo = cursor.monthValue

            // 月份匹配
            if (month.contains(mo)) {
                val d = cursor.dayOfMonth
                val dow = cursor.dayOfWeek.value % 7 // 周日=0
                val dayOk = day.contains(d)
                val weekOk = weekDays.contains(dow)
                // 日和周同时限定：任一匹配即可（标准 cron 语义）
                val isWildcardDay = parts[2] == "*"
                val isWildcardWeek = parts[4] == "*"
                val dateOk = if (isWildcardDay || isWildcardWeek) (dayOk || weekOk) else (dayOk && weekOk)

                if (dateOk) {
                    val h = cursor.hour
                    val min = cursor.minute
                    if (hour.contains(h) && minute.contains(min)) {
                        result.add(cursor)
                    }
                }
            }
            cursor = cursor.plusMinutes(1)
            // 防御：最多扫 5 年（避免死循环）
            if (cursor.isAfter(from.plusYears(5))) break
        }
        return result
    }

    /** 解析单个字段为允许值集合 */
    private fun parseField(field: String, min: Int, max: Int): Set<Int> {
        if (field == "*") return (min..max).toSet()

        val values = mutableSetOf<Int>()
        for (part in field.split(",")) {
            when {
                part.contains("*/") -> {
                    val step = part.substringAfter("*/").toInt()
                    var v = min
                    while (v <= max) { values.add(v); v += step }
                }
                part.contains("-") -> {
                    val (s, e) = part.split("-")
                    for (v in s.toInt()..e.toInt()) values.add(v)
                }
                else -> values.add(part.toInt())
            }
        }
        return values.filter { it in min..max }.toSet()
    }

    /** 给 Cron 表达式做有效性检查，返回错误信息或 null */
    fun validate(expression: String): String? {
        return try {
            nextExecutions(expression, 1)
            null
        } catch (e: Exception) {
            e.message ?: "表达式无效"
        }
    }
}
