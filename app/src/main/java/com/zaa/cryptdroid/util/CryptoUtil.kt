package com.zaa.cryptdroid.util

import org.bouncycastle.asn1.gm.GMNamedCurves
import org.bouncycastle.crypto.engines.SM2Engine
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.params.ParametersWithRandom
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.spec.ECGenParameterSpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * CryptoUtil — 对称加密 + RSA + SM2（BouncyCastle）
 */
object CryptoUtil {

    init {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    // ================= 对称加密 =================

    val SYMMETRIC_ALGOS = listOf(
        "AES" to false,
        "DES" to false,
        "3DES" to false,
        "Blowfish" to false,
        "RC4" to true,    // 流式，无 IV
        "Rabbit" to true  // 流式，无 IV
    )

    val MODES = listOf("ECB", "CBC", "CTR", "CFB", "OFB")

    private val random = SecureRandom()

    fun symmetricEncrypt(algo: String, mode: String, key: ByteArray, iv: ByteArray?, plaintext: ByteArray): ByteArray {
        val (_, isStream) = SYMMETRIC_ALGOS.first { it.first == algo }
        val cipher = buildCipher(algo, mode, isStream, Cipher.ENCRYPT_MODE, key, iv)
        return cipher.doFinal(plaintext)
    }

    fun symmetricDecrypt(algo: String, mode: String, key: ByteArray, iv: ByteArray?, ciphertext: ByteArray): ByteArray {
        val (_, isStream) = SYMMETRIC_ALGOS.first { it.first == algo }
        val cipher = buildCipher(algo, mode, isStream, Cipher.DECRYPT_MODE, key, iv)
        return cipher.doFinal(ciphertext)
    }

    private fun buildCipher(algo: String, mode: String, isStream: Boolean, opMode: Int, key: ByteArray, iv: ByteArray?): Cipher {
        val transformation = if (isStream) "$algo" else "$algo/$mode/PKCS5Padding"
        val cipher = Cipher.getInstance(transformation, "BC")
        val keySpec = SecretKeySpec(key, algo)
        if (isStream || mode == "ECB") {
            cipher.init(opMode, keySpec)
        } else {
            cipher.init(opMode, keySpec, IvParameterSpec(iv))
        }
        return cipher
    }

    // ================= RSA =================

    fun generateRsaKeyPair(): Pair<String, String> {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val kp = kpg.generateKeyPair()
        val pub = java.util.Base64.getEncoder().encodeToString(kp.public.encoded)
        val priv = java.util.Base64.getEncoder().encodeToString(kp.private.encoded)
        return "-----BEGIN PUBLIC KEY-----\n$pub\n-----END PUBLIC KEY-----" to
                "-----BEGIN PRIVATE KEY-----\n$priv\n-----END PRIVATE KEY-----"
    }

    fun rsaEncrypt(publicKeyPem: String, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, parsePublicKey(publicKeyPem))
        return cipher.doFinal(plaintext)
    }

    fun rsaDecrypt(privateKeyPem: String, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, parsePrivateKey(privateKeyPem))
        return cipher.doFinal(ciphertext)
    }

    fun rsaSign(privateKeyPem: String, data: ByteArray): ByteArray {
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initSign(parsePrivateKey(privateKeyPem))
        sig.update(data)
        return sig.sign()
    }

    fun rsaVerify(publicKeyPem: String, data: ByteArray, signature: ByteArray): Boolean {
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initVerify(parsePublicKey(publicKeyPem))
        sig.update(data)
        return sig.verify(signature)
    }

    private fun parsePublicKey(pem: String): PublicKey {
        val base64 = pem.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "").replace("\n", "").trim()
        val bytes = java.util.Base64.getDecoder().decode(base64)
        return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(bytes))
    }

    private fun parsePrivateKey(pem: String): PrivateKey {
        val base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "").replace("\n", "").trim()
        val bytes = java.util.Base64.getDecoder().decode(base64)
        return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(bytes))
    }

    // ================= SM2（国密）=================

    private fun sm2Domain(): ECDomainParameters {
        val x9 = GMNamedCurves.getByName("sm2p256v1")
        return ECDomainParameters(x9.curve, x9.g, x9.n, x9.h)
    }

    /** 生成 SM2 密钥对，返回 (公钥Hex, 私钥Hex)，各 64 字节 hex */
    fun generateSm2KeyPair(): Pair<String, String> {
        val kpg = KeyPairGenerator.getInstance("EC", "BC")
        kpg.initialize(ECGenParameterSpec("sm2p256v1"))
        val kp = kpg.generateKeyPair()
        val pubHex = (kp.public as ECPublicKey).q.getEncoded(false).toHex()
        val privHex = "%064x".format((kp.private as ECPrivateKey).d)
        return pubHex to privHex
    }

    /** SM2 加密，输入公钥 Hex（04||X||Y），返回密文 Hex */
    fun sm2Encrypt(publicKeyHex: String, plaintext: ByteArray): String {
        val domain = sm2Domain()
        val point = domain.curve.decodePoint(hexToBytes(publicKeyHex))
        val pubParams = ECPublicKeyParameters(point, domain)
        val engine = SM2Engine()
        engine.init(true, ParametersWithRandom(pubParams, random))
        return engine.processBlock(plaintext, 0, plaintext.size).toHex()
    }

    /** SM2 解密，输入私钥 Hex（64 字节），返回明文 */
    fun sm2Decrypt(privateKeyHex: String, ciphertextHex: String): ByteArray {
        val domain = sm2Domain()
        val privParams = ECPrivateKeyParameters(BigInteger(privateKeyHex, 16), domain)
        val engine = SM2Engine()
        engine.init(false, privParams)
        return engine.processBlock(hexToBytes(ciphertextHex), 0, ciphertextHex.length / 2)
    }

    /** Hex 字符串转 ByteArray */
    fun hexToBytes(hex: String): ByteArray {
        val cleaned = hex.trim().replace(" ", "")
        require(cleaned.length % 2 == 0) { "Hex 长度必须为偶数" }
        return ByteArray(cleaned.length / 2) { i ->
            cleaned.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
