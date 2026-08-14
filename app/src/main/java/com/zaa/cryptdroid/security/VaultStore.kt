package com.zaa.cryptdroid.security

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Arrays

/**
 * VaultStore — 密码本存储（本地密码信息维护，Kotlin 版）
 *
 * 完整安全协议：
 * ┌────────────────────────────────────────────────────────────┐
 * │  首次设置主密码                                              │
 * │  1. 生成随机盐 salt（32 字节）                                │
 * │  2. fingerprint = deriveKey(主密码, salt, 10000, 32)        │
 * │  3. 本地保存：salt + iterations + fingerprint（不含主密码！） │
 * │                                                            │
 * │  校验主密码                                                  │
 * │  4. verifyPassword(输入, salt, iterations, fingerprint)     │
 * │     → true 才能解锁密码本                                    │
 * │                                                            │
 * │  加密密码本数据                                              │
 * │  5. key = deriveKey(主密码, salt, iterations, 32)           │
 * │  6. 每次加密生成新 iv（12 字节随机）                           │
 * │  7. 密文 = aesGcmEncrypt(key, iv, JSON数据)                 │
 * │  8. 存储文件 = iv(12) ‖ 密文+tag                             │
 * └────────────────────────────────────────────────────────────┘
 *
 * 安全要点：
 *  - 主密码永不落盘，本地只存"指纹"（不可逆）
 *  - 密钥只在 native 堆内存中短暂存在，用完即清
 *  - 主密码遗忘 = 数据永久不可恢复（无服务器备份）
 */
class VaultStore(context: Context) {

    private val metaFile: File = File(context.filesDir, META_FILE)
    private val dataFile: File = File(context.filesDir, DATA_FILE)
    private val random = SecureRandom()

    /* 内存态（解锁后短暂持有） */
    private var salt: ByteArray? = null
    private var iterations: Int = 0
    private var fingerprint: ByteArray? = null

    /* ================= 主密码生命周期 ================= */

    /** 是否已设置主密码。 */
    fun isMasterPasswordSet(): Boolean = metaFile.exists()

    /** 首次设置主密码。成功后自动生成随机盐并写入元数据文件。 */
    @Throws(IOException::class)
    fun createMasterPassword(masterPassword: String) {
        val pass = masterPassword.toByteArray(StandardCharsets.UTF_8)

        // 1. 随机盐
        val newSalt = ByteArray(SALT_LEN).also(random::nextBytes)

        // 2. 派生指纹
        val fp = NativeCrypto.deriveKey(pass, newSalt, NativeCrypto.DEFAULT_ITERATIONS, NativeCrypto.KEY_LEN)

        // 3. 写文件：salt(32) ‖ iterations(4, 大端) ‖ fingerprint(32)
        try {
            writeMeta(newSalt, NativeCrypto.DEFAULT_ITERATIONS, fp)
            this.salt = newSalt
            this.iterations = NativeCrypto.DEFAULT_ITERATIONS
            this.fingerprint = fp
        } finally {
            pass.fill(0)
            fp.fill(0)
        }
    }

    /** 校验主密码（解锁密码本时调用）。@return true = 正确并解锁 */
    fun unlock(masterPassword: String): Boolean {
        if (!loadMetaIfNeeded()) return false
        val pass = masterPassword.toByteArray(StandardCharsets.UTF_8)
        return try {
            NativeCrypto.verifyPassword(pass, salt!!, iterations, fingerprint!!)
        } finally {
            pass.fill(0)
        }
    }

    /** 修改主密码：用旧密码解锁 → 用新密码重新加密现有密码本数据。 */
    fun changeMasterPassword(oldPassword: String, newPassword: String): Boolean {
        if (!unlock(oldPassword)) return false

        val plainData = loadPlainData(oldPassword) ?: return false
        return try {
            createMasterPassword(newPassword)
            val pass = newPassword.toByteArray(StandardCharsets.UTF_8)
            try {
                savePlainData(pass, plainData)
            } finally {
                pass.fill(0)
            }
            true
        } catch (e: IOException) {
            false
        } finally {
            plainData.fill(0)
        }
    }

    /** 锁定：清空内存中的密钥材料。 */
    fun lock() {
        salt?.fill(0)
        fingerprint?.fill(0)
        salt = null
        iterations = 0
        fingerprint = null
    }

    /* ================= 密码本数据读写 ================= */

    /** 读取明文密码本（需已设置主密码）。@return 明文 JSON；失败返回 null */
    fun loadPlainText(masterPassword: String): String? {
        if (!isMasterPasswordSet()) return null
        val plain = loadPlainData(masterPassword) ?: return null
        return try {
            String(plain, StandardCharsets.UTF_8)
        } finally {
            plain.fill(0)
        }
    }

    /** 写入明文密码本（需已设置主密码）。 */
    @Throws(IOException::class)
    fun savePlainText(masterPassword: String, jsonText: String) {
        val pass = masterPassword.toByteArray(StandardCharsets.UTF_8)
        val plain = jsonText.toByteArray(StandardCharsets.UTF_8)
        try {
            savePlainData(pass, plain)
        } finally {
            pass.fill(0)
            plain.fill(0)
        }
    }

    /* ================= 内部实现 ================= */

    /** 读取明文数据（内部：用主密码派生 key 解密数据文件） */
    private fun loadPlainData(masterPassword: String): ByteArray? {
        if (!loadMetaIfNeeded() || !dataFile.exists()) return null

        val pass = masterPassword.toByteArray(StandardCharsets.UTF_8)
        try {
            val key = NativeCrypto.deriveKey(pass, salt!!, iterations, NativeCrypto.KEY_LEN)
            try {
                val file = readFile(dataFile) ?: return null
                if (file.size < NativeCrypto.IV_LEN + 16) {
                    file.fill(0)
                    return null
                }
                // 拆分 iv(12) ‖ 密文+tag
                val iv = file.copyOfRange(0, NativeCrypto.IV_LEN)
                val cipher = file.copyOfRange(NativeCrypto.IV_LEN, file.size)
                val plain = NativeCrypto.aesGcmDecrypt(key, iv, cipher)
                iv.fill(0)
                cipher.fill(0)
                file.fill(0)
                return plain
            } finally {
                key.fill(0)
            }
        } finally {
            pass.fill(0)
        }
    }

    /** 保存明文数据（内部：加密后写文件 iv ‖ 密文+tag） */
    @Throws(IOException::class)
    private fun savePlainData(masterPasswordBytes: ByteArray, plain: ByteArray) {
        if (!loadMetaIfNeeded()) throw IOException("主密码未设置，无法写入密码本")

        val key = NativeCrypto.deriveKey(masterPasswordBytes, salt!!, iterations, NativeCrypto.KEY_LEN)
        try {
            // 每次加密必须用全新随机 iv
            val iv = ByteArray(NativeCrypto.IV_LEN).also(random::nextBytes)
            val cipher = NativeCrypto.aesGcmEncrypt(key, iv, plain)
                ?: throw IOException("AES-GCM 加密失败")

            // 拼接 iv ‖ 密文+tag 写盘
            val file = ByteArray(iv.size + cipher.size)
            System.arraycopy(iv, 0, file, 0, iv.size)
            System.arraycopy(cipher, 0, file, iv.size, cipher.size)

            writeFile(dataFile, file)

            iv.fill(0)
            cipher.fill(0)
            file.fill(0)
        } finally {
            key.fill(0)
        }
    }

    /** 懒加载元数据（salt + iterations + fingerprint） */
    private fun loadMetaIfNeeded(): Boolean {
        if (salt != null && fingerprint != null) return true
        if (!metaFile.exists()) return false

        val file = readFile(metaFile) ?: return false
        if (file.size != SALT_LEN + 4 + FINGERPRINT_LEN) {
            file.fill(0)
            return false
        }

        this.salt = file.copyOfRange(0, SALT_LEN)
        this.iterations = ((file[SALT_LEN].toInt() and 0xFF) shl 24) or
                ((file[SALT_LEN + 1].toInt() and 0xFF) shl 16) or
                ((file[SALT_LEN + 2].toInt() and 0xFF) shl 8) or
                (file[SALT_LEN + 3].toInt() and 0xFF)
        this.fingerprint = file.copyOfRange(SALT_LEN + 4, file.size)
        file.fill(0)
        return true
    }

    private fun writeMeta(newSalt: ByteArray, iter: Int, fp: ByteArray) {
        val file = ByteArray(SALT_LEN + 4 + FINGERPRINT_LEN)
        System.arraycopy(newSalt, 0, file, 0, SALT_LEN)
        file[SALT_LEN] = ((iter shr 24) and 0xFF).toByte()
        file[SALT_LEN + 1] = ((iter shr 16) and 0xFF).toByte()
        file[SALT_LEN + 2] = ((iter shr 8) and 0xFF).toByte()
        file[SALT_LEN + 3] = (iter and 0xFF).toByte()
        System.arraycopy(fp, 0, file, SALT_LEN + 4, FINGERPRINT_LEN)
        writeFile(metaFile, file)
        file.fill(0)
    }

    private fun writeFile(f: File, data: ByteArray) {
        FileOutputStream(f).use { it.write(data) }
    }

    private fun readFile(f: File): ByteArray? {
        return try {
            FileInputStream(f).use { fis ->
                val data = ByteArray(f.length().toInt())
                val read = fis.read(data)
                if (read != data.size) {
                    data.fill(0)
                    null
                } else {
                    data
                }
            }
        } catch (e: IOException) {
            null
        }
    }

    companion object {
        private const val META_FILE = "vault_meta.bin"
        private const val DATA_FILE = "vault_data.bin"
        private const val SALT_LEN = 32
        private const val FINGERPRINT_LEN = NativeCrypto.VERIFY_FINGERPRINT_LEN
    }
}
