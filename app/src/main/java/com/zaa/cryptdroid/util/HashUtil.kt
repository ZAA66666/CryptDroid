package com.zaa.cryptdroid.util

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * HashUtil — 哈希与 HMAC 计算
 * 算法与 PWA 版对应：MD5/SHA-1/224/256/384/512/RIPEMD-160/SHA3 系列/HMAC 系列。
 * 说明：部分算法（SHA-224/384、RIPEMD-160、SHA3）Android 系统 MessageDigest 可能不支持，
 *      支持时用系统实现，否则抛异常由上层提示。
 */
object HashUtil {

    /** 支持的算法列表（key = 显示名，value = JDK 算法名） */
    val ALGORITHMS = listOf(
        "MD5" to "MD5",
        "SHA-1" to "SHA-1",
        "SHA-224" to "SHA-224",
        "SHA-256" to "SHA-256",
        "SHA-384" to "SHA-384",
        "SHA-512" to "SHA-512",
        "SHA3-256" to "SHA3-256",
        "SHA3-512" to "SHA3-512",
        "RIPEMD-160" to "RIPEMD160"
    )

    /** HMAC 算法列表 */
    val HMAC_ALGORITHMS = listOf(
        "HMAC-MD5" to "HmacMD5",
        "HMAC-SHA1" to "HmacSHA1",
        "HMAC-SHA256" to "HmacSHA256",
        "HMAC-SHA512" to "HmacSHA512"
    )

    /** 计算哈希（无密钥） */
    fun hash(text: String, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return bytes.toHex()
    }

    /** 计算 HMAC（带密钥） */
    fun hmac(text: String, key: String, algorithm: String): String {
        val mac = Mac.getInstance(algorithm)
        val keySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), algorithm)
        mac.init(keySpec)
        val bytes = mac.doFinal(text.toByteArray(Charsets.UTF_8))
        return bytes.toHex()
    }
}

/** ByteArray → 小写 hex 字符串（顶层扩展函数，供全项目使用） */
fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
