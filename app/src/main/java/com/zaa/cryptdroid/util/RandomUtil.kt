package com.zaa.cryptdroid.util

import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RandomUtil — 随机文本 / 密码生成 / 虚假数据
 */
object RandomUtil {

    private val random = SecureRandom()

    // 字符集
    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    /** 生成随机密码 */
    fun password(length: Int, useUpper: Boolean, useDigits: Boolean, useSymbols: Boolean): String {
        var pool = LOWER
        if (useUpper) pool += UPPER
        if (useDigits) pool += DIGITS
        if (useSymbols) pool += SYMBOLS
        return randomString(length, pool)
    }

    /** 生成纯数字字符串 */
    fun digits(length: Int): String {
        val sb = StringBuilder(length)
        repeat(length) { sb.append(DIGITS[random.nextInt(DIGITS.length)]) }
        return sb.toString()
    }

    /** 随机字符串（自定义字符池） */
    fun randomString(length: Int, pool: String): String {
        require(length > 0)
        val sb = StringBuilder(length)
        repeat(length) { sb.append(pool[random.nextInt(pool.length)]) }
        return sb.toString()
    }

    /** 随机 UUID */
    fun uuid(): String = java.util.UUID.randomUUID().toString()

    // ---------- 虚假数据 ----------

    private val SURNAMES = arrayOf("赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨")
    private val GIVEN_NAMES = arrayOf("伟", "芳", "娜", "敏", "静", "磊", "军", "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "霞")
    private val CITIES = arrayOf("北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "南京", "西安", "重庆")
    private val EMAIL_DOMAINS = arrayOf("qq.com", "163.com", "gmail.com", "126.com", "foxmail.com", "outlook.com")

    /** 随机中文姓名 */
    fun name(): String = SURNAMES[random.nextInt(SURNAMES.size)] + GIVEN_NAMES[random.nextInt(GIVEN_NAMES.size)]

    /** 随机手机号（1 开头） */
    fun phone(): String = "1" + random.nextInt(10).toString() +
            (3 + random.nextInt(7)).toString() + digits(8)

    /** 随机邮箱 */
    fun email(): String = randomString(8, LOWER + DIGITS) + "@" + EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.size)]

    /** 随机城市 */
    fun city(): String = CITIES[random.nextInt(CITIES.size)]

    /** 随机日期（YYYY-MM-DD） */
    fun date(startYear: Int = 1970, endYear: Int = 2025): String {
        val year = startYear + random.nextInt(endYear - startYear + 1)
        val month = 1 + random.nextInt(12)
        val day = 1 + random.nextInt(28)
        return "%04d-%02d-%02d".format(year, month, day)
    }

    /** 随机 IP */
    fun ip(): String = "${random.nextInt(256)}.${random.nextInt(256)}.${random.nextInt(256)}.${random.nextInt(256)}"

    /** 当前时间戳（秒 / 毫秒） */
    fun timestamp(millis: Boolean = false): String =
        if (millis) System.currentTimeMillis().toString() else (System.currentTimeMillis() / 1000).toString()

    /** 时间戳转日期 */
    fun timestampToDate(seconds: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val fmt = SimpleDateFormat(pattern, Locale.getDefault())
        return fmt.format(Date(seconds.toLong() * 1000))
    }
}
