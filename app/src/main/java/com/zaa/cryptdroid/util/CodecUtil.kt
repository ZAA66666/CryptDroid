package com.zaa.cryptdroid.util

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * CodecUtil — 编解码工具
 * 与 PWA 版对应：Base64/Base32/Base58/Hex/URL/Unicode 转义/大小写转换。
 */
object CodecUtil {

    // ---------- Base64 ----------
    fun base64Encode(text: String): String =
        Base64.encodeToString(text.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

    fun base64Decode(text: String): String {
        val bytes = Base64.decode(text.trim(), Base64.NO_WRAP)
        return String(bytes, StandardCharsets.UTF_8)
    }

    // ---------- Base32（RFC 4648）----------
    private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun base32Encode(text: String): String {
        val input = text.toByteArray(StandardCharsets.UTF_8)
        val sb = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (b in input) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                val index = (buffer shr (bitsLeft - 5)) and 0x1F
                sb.append(BASE32_ALPHABET[index])
                bitsLeft -= 5
            }
        }
        if (bitsLeft > 0) {
            val index = (buffer shl (5 - bitsLeft)) and 0x1F
            sb.append(BASE32_ALPHABET[index])
        }
        return sb.toString()
    }

    fun base32Decode(text: String): String {
        val cleaned = text.trim().uppercase().replace("=", "")
        val output = java.io.ByteArrayOutputStream()
        var buffer = 0
        var bitsLeft = 0
        for (c in cleaned) {
            val value = BASE32_ALPHABET.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                output.write((buffer shr (bitsLeft - 8)) and 0xFF)
                bitsLeft -= 8
            }
        }
        return String(output.toByteArray(), StandardCharsets.UTF_8)
    }

    // ---------- Base58（比特币字母表）----------
    private const val BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

    fun base58Encode(text: String): String {
        val input = text.toByteArray(StandardCharsets.UTF_8)
        var zeros = 0
        while (zeros < input.size && input[zeros] == 0.toByte()) zeros++
        var num = java.math.BigInteger(1, input)
        val sb = StringBuilder()
        while (num > java.math.BigInteger.ZERO) {
            val qr = num.divideAndRemainder(java.math.BigInteger.valueOf(58))
            sb.append(BASE58_ALPHABET[qr[1].toInt()])
            num = qr[0]
        }
        repeat(zeros) { sb.append('1') }
        return sb.reverse().toString()
    }

    fun base58Decode(text: String): String {
        val cleaned = text.trim()
        var num = java.math.BigInteger.ZERO
        val base = java.math.BigInteger.valueOf(58)
        for (c in cleaned) {
            val idx = BASE58_ALPHABET.indexOf(c)
            if (idx < 0) continue
            num = num.multiply(base).add(java.math.BigInteger.valueOf(idx.toLong()))
        }
        var bytes = num.toByteArray()
        if (bytes.size > 1 && bytes[0] == 0.toByte()) bytes = bytes.copyOfRange(1, bytes.size)
        return String(bytes, StandardCharsets.UTF_8)
    }

    // ---------- Hex ----------
    fun hexEncode(text: String): String = text.toByteArray(Charsets.UTF_8).toHex()

    fun hexDecode(text: String): String {
        val cleaned = text.trim().replace(" ", "")
        require(cleaned.length % 2 == 0) { "Hex 字符串长度必须为偶数" }
        val bytes = ByteArray(cleaned.length / 2)
        for (i in bytes.indices) {
            bytes[i] = cleaned.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return String(bytes, Charsets.UTF_8)
    }

    // ---------- URL ----------
    fun urlEncode(text: String): String = URLEncoder.encode(text, "UTF-8")

    fun urlDecode(text: String): String = URLDecoder.decode(text, "UTF-8")

    // ---------- Unicode 转义 ----------
    fun unicodeEncode(text: String): String =
        text.map { if (it.code > 127) "\\u%04x".format(it.code) else it.toString() }.joinToString("")

    fun unicodeDecode(text: String): String {
        val regex = Regex("\\\\u([0-9a-fA-F]{4})")
        return regex.replace(text) { m -> m.groupValues[1].toInt(16).toChar().toString() }
    }

    // ---------- 大小写 ----------
    fun toUpper(text: String): String = text.uppercase()
    fun toLower(text: String): String = text.lowercase()
    fun toTitle(text: String): String = text.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}
