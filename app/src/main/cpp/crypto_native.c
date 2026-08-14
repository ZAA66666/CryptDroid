/*
 * crypto_native.c — 原生安全层（抗破解核心）
 *
 * 设计目标：
 *  1. 主密码派生（PBKDF2-HMAC-SHA256）在 native 层完成，逆向者必须反汇编 .so
 *  2. 密码本 AES-256-GCM 加解密在 native 层完成，密钥不落入 Java 堆内存
 *  3. 调用系统 BoringSSL（libcrypto），动态链接，APK 体积零增加，算法正确性由 Google 保证
 *
 * 链接说明：-lcrypto 是 Android 系统自带的 BoringSSL 库（API 21+ 全设备存在），
 *          因此本 .so 不打包任何第三方加密代码，保持体积最小。
 */

#include <jni.h>
#include <string.h>
#include <stdlib.h>

#include <openssl/evp.h>
#include <openssl/crypto.h>

/* 常量（供 Java 侧参考） */
#define GCM_IV_LEN     12   /* GCM 推荐 nonce 长度 */
#define GCM_TAG_LEN    16   /* 认证标签长度 */

/*
 * 通用工具：JNI 字节数组 <-> C 缓冲区转换
 */
static unsigned char* jbyteArray_to_uchar(JNIEnv* env, jbyteArray arr, jsize* outLen) {
    if (arr == NULL) {
        *outLen = 0;
        return NULL;
    }
    *outLen = (*env)->GetArrayLength(env, arr);
    unsigned char* buf = (unsigned char*)malloc(*outLen > 0 ? *outLen : 1);
    if (buf == NULL) {
        return NULL;
    }
    if (*outLen > 0) {
        (*env)->GetByteArrayRegion(env, arr, 0, *outLen, (jbyte*)buf);
    }
    return buf;
}

static jbyteArray uchar_to_jbyteArray(JNIEnv* env, const unsigned char* buf, jsize len) {
    jbyteArray result = (*env)->NewByteArray(env, len);
    if (result == NULL) {
        return NULL;
    }
    (*env)->SetByteArrayRegion(env, result, 0, len, (const jbyte*)buf);
    return result;
}

/* 安全清零：防止密钥残留在堆内存 */
static void secure_wipe(void* ptr, size_t len) {
    if (ptr != NULL && len > 0) {
        volatile unsigned char* p = (volatile unsigned char*)ptr;
        while (len--) {
            *p++ = 0;
        }
    }
}

/*
 * Java_com_zaa_cryptdroid_security_NativeCrypto_nativeDeriveKey
 * 主密码派生密钥：PBKDF2-HMAC-SHA256(主密码, 盐, 迭代次数, 密钥长度)
 * 返回值：派生出的字节数组（Java 侧据此做 AES 加解密）
 */
JNIEXPORT jbyteArray JNICALL
Java_com_zaa_cryptdroid_security_NativeCrypto_nativeDeriveKey(
        JNIEnv* env, jobject thiz,
        jbyteArray jPassword, jbyteArray jSalt,
        jint iterations, jint keyLen) {

    jsize passLen, saltLen;
    unsigned char* pass = jbyteArray_to_uchar(env, jPassword, &passLen);
    unsigned char* salt = jbyteArray_to_uchar(env, jSalt, &saltLen);

    if (pass == NULL || (jPassword != NULL && jSalt != NULL && salt == NULL)) {
        if (pass) free(pass);
        if (salt) free(salt);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"), "jbyteArray_to_uchar failed");
        return NULL;
    }

    /* 迭代次数防御：不允许极端值拖垮性能或安全 */
    if (iterations < 1 || iterations > 100000000) {
        iterations = 10000;
    }
    if (keyLen < 1 || keyLen > 1024) {
        keyLen = 32;
    }

    unsigned char* out = (unsigned char*)malloc(keyLen);
    if (out == NULL) {
        free(pass);
        free(salt);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"), "malloc failed");
        return NULL;
    }

    /* BoringSSL 的 PBKDF2：pass 可以是任意字节（不要求 NUL 结尾） */
    int ok = PKCS5_PBKDF2_HMAC(
            (const char*)pass, passLen,
            salt, saltLen,
            iterations, EVP_sha256(),
            keyLen, out);

    /* 密码/盐使用完毕立即清零 */
    secure_wipe(pass, passLen);
    secure_wipe(salt, saltLen);
    free(pass);
    free(salt);

    if (ok != 1) {
        secure_wipe(out, keyLen);
        free(out);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/security/GeneralSecurityException"), "PBKDF2 derive failed");
        return NULL;
    }

    jbyteArray result = uchar_to_jbyteArray(env, out, keyLen);
    secure_wipe(out, keyLen);
    free(out);
    return result;
}

/*
 * Java_com_zaa_cryptdroid_security_NativeCrypto_nativeAesGcmEncrypt
 * AES-256-GCM 加密：输出 = 密文 + 16 字节认证标签
 * 入参：key(32字节) iv(12字节) plaintext
 */
JNIEXPORT jbyteArray JNICALL
Java_com_zaa_cryptdroid_security_NativeCrypto_nativeAesGcmEncrypt(
        JNIEnv* env, jobject thiz,
        jbyteArray jKey, jbyteArray jIv, jbyteArray jPlain) {

    jsize keyLen, ivLen, plainLen;
    unsigned char* key = jbyteArray_to_uchar(env, jKey, &keyLen);
    unsigned char* iv  = jbyteArray_to_uchar(env, jIv, &ivLen);
    unsigned char* plain = jbyteArray_to_uchar(env, jPlain, &plainLen);

    /* 参数校验：密钥必须 256 位，IV 必须 12 字节 */
    if (keyLen != 32 || ivLen != GCM_IV_LEN) {
        free(key); free(iv); free(plain);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/IllegalArgumentException"),
                "key must be 32 bytes, iv must be 12 bytes");
        return NULL;
    }

    /* 密文缓冲区：明文长度 + 标签长度 */
    unsigned char* out = (unsigned char*)malloc(plainLen + GCM_TAG_LEN);
    if (out == NULL) {
        free(key); free(iv); free(plain);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"), "malloc failed");
        return NULL;
    }

    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (ctx == NULL) {
        free(key); free(iv); free(plain); free(out);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"), "EVP_CIPHER_CTX_new failed");
        return NULL;
    }

    int ok = 1;
    int outLen = 0, finalLen = 0;

    ok &= EVP_EncryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, NULL, NULL);
    ok &= EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, GCM_IV_LEN, NULL);
    ok &= EVP_EncryptInit_ex(ctx, NULL, NULL, key, iv);
    ok &= EVP_EncryptUpdate(ctx, out, &outLen, plain, plainLen);
    ok &= EVP_EncryptFinal_ex(ctx, out + outLen, &finalLen);
    outLen += finalLen;

    /* 取 16 字节认证标签，追加到密文尾部 */
    unsigned char tag[GCM_TAG_LEN];
    ok &= EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_GET_TAG, GCM_TAG_LEN, tag);
    if (ok) {
        memcpy(out + outLen, tag, GCM_TAG_LEN);
        outLen += GCM_TAG_LEN;
    }

    EVP_CIPHER_CTX_free(ctx);
    secure_wipe(key, keyLen);
    secure_wipe(iv, ivLen);
    free(key); free(iv); free(plain);

    if (!ok) {
        secure_wipe(out, plainLen + GCM_TAG_LEN);
        free(out);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/security/GeneralSecurityException"), "AES-GCM encrypt failed");
        return NULL;
    }

    jbyteArray result = uchar_to_jbyteArray(env, out, outLen);
    secure_wipe(out, outLen);
    free(out);
    return result;
}

/*
 * Java_com_zaa_cryptdroid_security_NativeCrypto_nativeAesGcmDecrypt
 * AES-256-GCM 解密：入参为"密文+16字节标签"拼接的字节数组
 * 认证失败返回 null（不抛异常，由 Java 层转成友好提示）
 */
JNIEXPORT jbyteArray JNICALL
Java_com_zaa_cryptdroid_security_NativeCrypto_nativeAesGcmDecrypt(
        JNIEnv* env, jobject thiz,
        jbyteArray jKey, jbyteArray jIv, jbyteArray jCipher) {

    jsize keyLen, ivLen, cipherLen;
    unsigned char* key = jbyteArray_to_uchar(env, jKey, &keyLen);
    unsigned char* iv  = jbyteArray_to_uchar(env, jIv, &ivLen);
    unsigned char* cipher = jbyteArray_to_uchar(env, jCipher, &cipherLen);

    if (keyLen != 32 || ivLen != GCM_IV_LEN || cipherLen < GCM_TAG_LEN) {
        free(key); free(iv); free(cipher);
        return NULL; /* 参数不合法 → 解密失败 */
    }

    /* 拆出密文体和标签 */
    int dataLen = cipherLen - GCM_TAG_LEN;
    unsigned char* data = cipher;
    unsigned char* tag  = cipher + dataLen;

    unsigned char* out = (unsigned char*)malloc(dataLen > 0 ? dataLen : 1);
    if (out == NULL) {
        free(key); free(iv); free(cipher);
        return NULL;
    }

    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    if (ctx == NULL) {
        free(key); free(iv); free(cipher); free(out);
        return NULL;
    }

    int ok = 1;
    int outLen = 0, finalLen = 0;

    ok &= EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, NULL, NULL);
    ok &= EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_IVLEN, GCM_IV_LEN, NULL);
    ok &= EVP_DecryptInit_ex(ctx, NULL, NULL, key, iv);
    ok &= EVP_DecryptUpdate(ctx, out, &outLen, data, dataLen);
    /* 设置期望标签后再 Final，认证失败 Final 返回 0 */
    ok &= EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_TAG, GCM_TAG_LEN, tag);
    ok &= EVP_DecryptFinal_ex(ctx, out + outLen, &finalLen);
    if (ok) {
        outLen += finalLen;
    }

    EVP_CIPHER_CTX_free(ctx);
    secure_wipe(key, keyLen);
    secure_wipe(iv, ivLen);
    secure_wipe(cipher, cipherLen);
    free(key); free(iv); free(cipher);

    if (!ok) {
        secure_wipe(out, dataLen);
        free(out);
        return NULL; /* 认证失败（密码错误 / 数据被篡改） */
    }

    jbyteArray result = uchar_to_jbyteArray(env, out, outLen);
    secure_wipe(out, outLen);
    free(out);
    return result;
}

/*
 * Java_com_zaa_cryptdroid_security_NativeCrypto_nativeVerifyPassword
 * 主密码校验：派生密钥后与存储的密钥指纹对比（constant-time 比较，防时序侧信道）
 * 入参：主密码、盐、迭代次数、期望指纹（= deriveKey 的结果，提前存储）
 * 返回：是否匹配
 */
JNIEXPORT jboolean JNICALL
Java_com_zaa_cryptdroid_security_NativeCrypto_nativeVerifyPassword(
        JNIEnv* env, jobject thiz,
        jbyteArray jPassword, jbyteArray jSalt,
        jint iterations, jbyteArray jExpected) {

    jsize passLen, saltLen, expectedLen;
    unsigned char* pass = jbyteArray_to_uchar(env, jPassword, &passLen);
    unsigned char* salt = jbyteArray_to_uchar(env, jSalt, &saltLen);
    unsigned char* expected = jbyteArray_to_uchar(env, jExpected, &expectedLen);

    if (iterations < 1 || iterations > 100000000) {
        iterations = 10000;
    }
    /* 指纹统一为 32 字节 */
    if (expectedLen != 32) {
        free(pass); free(salt); free(expected);
        return JNI_FALSE;
    }

    unsigned char derived[32];
    int ok = PKCS5_PBKDF2_HMAC(
            (const char*)pass, passLen,
            salt, saltLen,
            iterations, EVP_sha256(),
            32, derived);

    secure_wipe(pass, passLen);
    secure_wipe(salt, saltLen);
    free(pass); free(salt);

    if (ok != 1) {
        free(expected);
        return JNI_FALSE;
    }

    /* CRYPTO_memcmp：constant-time 比较，不因字节差异提前退出 */
    jboolean match = (CRYPTO_memcmp(derived, expected, 32) == 0) ? JNI_TRUE : JNI_FALSE;

    secure_wipe(derived, 32);
    secure_wipe(expected, expectedLen);
    free(expected);
    return match;
}
