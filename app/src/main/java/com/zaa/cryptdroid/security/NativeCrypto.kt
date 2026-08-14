package com.zaa.cryptdroid.security

/**
 * NativeCrypto — 原生加密桥（JNI 封装，Kotlin 版）
 *
 * 职责：把"主密码派生 + 密码本加解密"调用转发到 native 层（crypto_native.so）。
 * 为什么下沉到 native？
 *  - Java/Kotlin 字节码可被反编译，逆向者能直接读逻辑、下断点。
 *  - native 是 ARM 机器码，反汇编难度高一个量级，且本类不保存任何密钥材料，
 *    主密码派生出的密钥仅存在于 C 堆内存中（用完即清）。
 *
 * 安全协议（配合 VaultStore 使用）：
 *  1. 首次设置主密码：deriveKey(主密码, 随机盐, 10000次, 32字节) → 得到「指纹」存本地
 *  2. 校验主密码：verifyPassword(输入, 盐, 迭代, 指纹) → 正确才允许访问密码本
 *  3. 密码本数据：aesGcmEncrypt(派生密钥, iv, JSON) 后存文件，解密时 aesGcmDecrypt
 *
 * ⚠️ 本类不缓存任何密钥/密码，全部逐次传入 native，用完即弃。
 * ⚠️ Kotlin object 的 external 方法是实例方法，JNI 第二参数是 jobject（C 层已对应修改）。
 */
object NativeCrypto {

    /** 常量：与 C 层一致 */
    const val KEY_LEN = 32
    const val IV_LEN = 12
    const val DEFAULT_ITERATIONS = 10000
    const val VERIFY_FINGERPRINT_LEN = 32

    init {
        // 加载 libcrypto_native.so（CMake 编译产物）
        System.loadLibrary("crypto_native")
    }

    /**
     * 主密码派生密钥：PBKDF2-HMAC-SHA256。
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
     * 主密码校验（constant-time）。
     *
     * @param expected 存储的指纹（首次 deriveKey 的输出，32 字节）
     */
    fun verifyPassword(password: ByteArray, salt: ByteArray, iterations: Int, expected: ByteArray): Boolean {
        if (expected.size != VERIFY_FINGERPRINT_LEN) return false
        return nativeVerifyPassword(password, salt, iterations, expected)
    }

    /**
     * AES-256-GCM 加密。输出 = 密文 ‖ 16 字节认证标签。
     *
     * @param key  32 字节密钥（来自 deriveKey）
     * @param iv   12 字节随机 nonce（每次加密必须重新随机！）
     */
    fun aesGcmEncrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray {
        return nativeAesGcmEncrypt(key, iv, plaintext)
    }

    /**
     * AES-256-GCM 解密。
     *
     * @param cipher 密文+16 字节标签（aesGcmEncrypt 的输出原样传入）
     * @return 明文；密码错误或数据被篡改时返回 null
     */
    fun aesGcmDecrypt(key: ByteArray, iv: ByteArray, cipher: ByteArray): ByteArray? {
        if (cipher.size < 16) return null
        return nativeAesGcmDecrypt(key, iv, cipher)
    }

    /* ---------- native 方法声明（对应 crypto_native.c，方法名不可改） ---------- */

    private external fun nativeDeriveKey(password: ByteArray, salt: ByteArray, iterations: Int, keyLen: Int): ByteArray

    private external fun nativeVerifyPassword(password: ByteArray, salt: ByteArray, iterations: Int, expected: ByteArray): Boolean

    private external fun nativeAesGcmEncrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray

    private external fun nativeAesGcmDecrypt(key: ByteArray, iv: ByteArray, cipher: ByteArray): ByteArray?
}
