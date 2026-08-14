package com.zaa.cryptdroid.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * NativeCrypto — 原生加密桥（JNI 封装，Kotlin 版）
 *
 * 分层策略（安全架构详见 HANDOVER.md 4.5 节）：
 *  ┌──────────────────────────────────────────────────────┐
 *  │ native 层（crypto_native.so，纯 C 自实现）             │
 *  │  - PBKDF2-HMAC-SHA256 主密码派生（抗暴力破解核心）      │
 *  │  - 主密码校验（constant-time）                        │
 *  │  - 不依赖系统加密库（NDK r26+ 已移除 BoringSSL 头文件） │
 *  ├──────────────────────────────────────────────────────┤
 *  │ Java 层（javax.crypto，Android 系统自带）             │
 *  │  - AES-256-GCM 密码本加解密                          │
 *  │  - 密钥来自 native 派生，用完即清                      │
 *  └──────────────────────────────────────────────────────┘
 *
 * 安全协议（配合 VaultStore 使用）：
 *  1. 首次设置主密码：deriveKey(主密码, 随机盐, 10000次, 32字节) → 「指纹」存本地
 *  2. 校验主密码：verifyPassword(输入, 盐, 迭代, 指纹) → true 才允许访问密码本
 *  3. 密码本数据：aesGcmEncrypt(派生密钥, iv, JSON) 后存文件，解密时 aesGcmDecrypt
 *
 * ⚠️ 本类不缓存任何密钥/密码，全部逐次传入，用完即清。
 */
object NativeCrypto {

    /** 常量 */
    const val KEY_LEN = 32                     // AES-256 密钥字节数
    const val IV_LEN = 12                      // GCM 推荐 nonce 长度
    const val DEFAULT_ITERATIONS = 10000       // PBKDF2 迭代次数
    const val VERIFY_FINGERPRINT_LEN = 32      // 主密码指纹长度

    private val random = SecureRandom()

    init {
        // 加载 libcrypto_native.so（CMake 编译产物）
        System.loadLibrary("crypto_native")
    }

    /* ================= 主密码派生 / 校验（native） ================= */

    /**
     * 主密码派生密钥：PBKDF2-HMAC-SHA256（native 层执行）。
     *
     * @param password   主密码字节（UTF-8）
     * @param salt       随机盐（至少 16 字节，建议 32）
     * @param iterations PBKDF2 迭代次数（越高越难暴力破解，越慢）
     * @param keyLen     派生密钥长度（32 = AES-256）
     * @return 派生密钥；失败抛异常
     */
    fun deriveKey(password: ByteArray, salt: ByteArray, iterations: Int, keyLen: Int): ByteArray {
        return nativeDeriveKey(password, salt, iterations, keyLen)
    }

    /**
     * 主密码校验（native 层 constant-time 比较）。
     *
     * @param expected 存储的指纹（首次 deriveKey 的输出，32 字节）
     */
    fun verifyPassword(password: ByteArray, salt: ByteArray, iterations: Int, expected: ByteArray): Boolean {
        if (expected.size != VERIFY_FINGERPRINT_LEN) return false
        return nativeVerifyPassword(password, salt, iterations, expected)
    }

    /* ================= 密码本加解密（Java javax.crypto） ================= */

    /**
     * AES-256-GCM 加密。输出 = 密文 ‖ 16 字节认证标签。
     *
     * @param key  32 字节密钥（来自 deriveKey）
     * @param iv   12 字节随机 nonce（每次加密必须重新随机！传 null 则自动生成）
     */
    fun aesGcmEncrypt(key: ByteArray, iv: ByteArray?, plaintext: ByteArray): ByteArray {
        require(key.size == KEY_LEN) { "key 必须 32 字节" }
        val nonce = iv ?: ByteArray(IV_LEN).also { random.nextBytes(it) }
        require(nonce.size == IV_LEN) { "iv 必须 12 字节" }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
        val ciphertext = cipher.doFinal(plaintext)

        // 返回 iv ‖ 密文+tag 拼接（方便存储）
        val result = ByteArray(nonce.size + ciphertext.size)
        System.arraycopy(nonce, 0, result, 0, nonce.size)
        System.arraycopy(ciphertext, 0, result, nonce.size, ciphertext.size)
        return result
    }

    /**
     * AES-256-GCM 解密。
     *
     * @param input 入参 = iv(12) ‖ 密文+tag（aesGcmEncrypt 的完整输出）
     * @return 明文；密码错误或数据被篡改时返回 null
     */
    fun aesGcmDecrypt(key: ByteArray, input: ByteArray): ByteArray? {
        if (key.size != KEY_LEN || input.size < IV_LEN + 16) return null

        val nonce = input.copyOfRange(0, IV_LEN)
        val ciphertext = input.copyOfRange(IV_LEN, input.size)

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            null  // 认证失败（密码错误 / 数据被篡改）
        }
    }

    /* ---------- native 方法声明（对应 crypto_native.c，方法名不可改） ---------- */

    private external fun nativeDeriveKey(password: ByteArray, salt: ByteArray, iterations: Int, keyLen: Int): ByteArray

    private external fun nativeVerifyPassword(password: ByteArray, salt: ByteArray, iterations: Int, expected: ByteArray): Boolean
}
