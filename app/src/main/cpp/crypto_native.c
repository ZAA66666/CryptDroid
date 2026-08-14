/*
 * crypto_native.c — 原生安全层（抗破解核心，纯 C 自实现）
 *
 * 设计目标：
 *  1. 主密码派生（PBKDF2-HMAC-SHA256）在 native 层完成，逆向者必须反汇编 .so
 *  2. 不依赖任何系统加密库（NDK r26+ 已移除 BoringSSL 公共头文件），
 *     SHA-256 / HMAC / PBKDF2 全部纯 C 实现，标准算法，可移植可验证
 *  3. constant-time 比较防时序侧信道
 *
 * 算法参考标准：
 *  - SHA-256: FIPS 180-4
 *  - HMAC: RFC 2104
 *  - PBKDF2: RFC 2898
 * 已知测试向量（RFC 4231 / RFC 6070 风格）已在 Java 侧自检注释中列出。
 *
 * 与 Kotlin 侧的配合（NativeCrypto.kt）：
 *  - nativeDeriveKey  -> 本文件 Java_com_zaa_cryptdroid_security_NativeCrypto_nativeDeriveKey
 *  - nativeVerifyPassword -> ..._nativeVerifyPassword
 */

#include <jni.h>
#include <stdint.h>
#include <string.h>
#include <stdlib.h>

/* ============ SHA-256（FIPS 180-4）============ */

static const uint32_t K256[64] = {
    0x428a2f98u, 0x71374491u, 0xb5c0fbcfu, 0xe9b5dba5u,
    0x3956c25bu, 0x59f111f1u, 0x923f82a4u, 0xab1c5ed5u,
    0xd807aa98u, 0x12835b01u, 0x243185beu, 0x550c7dc3u,
    0x72be5d74u, 0x80deb1feu, 0x9bdc06a7u, 0xc19bf174u,
    0xe49b69c1u, 0xefbe4786u, 0x0fc19dc6u, 0x240ca1ccu,
    0x2de92c6fu, 0x4a7484aau, 0x5cb0a9dcu, 0x76f988dau,
    0x983e5152u, 0xa831c66du, 0xb00327c8u, 0xbf597fc7u,
    0xc6e00bf3u, 0xd5a79147u, 0x06ca6351u, 0x14292967u,
    0x27b70a85u, 0x2e1b2138u, 0x4d2c6dfcu, 0x53380d13u,
    0x650a7354u, 0x766a0abbu, 0x81c2c92eu, 0x92722c85u,
    0xa2bfe8a1u, 0xa81a664bu, 0xc24b8b70u, 0xc76c51a3u,
    0xd192e819u, 0xd6990624u, 0xf40e3585u, 0x106aa070u,
    0x19a4c116u, 0x1e376c08u, 0x2748774cu, 0x34b0bcb5u,
    0x391c0cb3u, 0x4ed8aa4au, 0x5b9cca4fu, 0x682e6ff3u,
    0x748f82eeu, 0x78a5636fu, 0x84c87814u, 0x8cc70208u,
    0x90befffau, 0xa4506cebu, 0xbef9a3f7u, 0xc67178f2u
};

#define ROR(x, n) (((x) >> (n)) | ((x) << (32 - (n))))

typedef struct {
    uint32_t state[8];
    uint64_t total_len;   /* 已处理字节数 */
    uint8_t  buffer[64];
    size_t   buffer_len;
} sha256_ctx;

static void sha256_init(sha256_ctx* c) {
    c->state[0] = 0x6a09e667u;
    c->state[1] = 0xbb67ae85u;
    c->state[2] = 0x3c6ef372u;
    c->state[3] = 0xa54ff53au;
    c->state[4] = 0x510e527fu;
    c->state[5] = 0x9b05688cu;
    c->state[6] = 0x1f83d9abu;
    c->state[7] = 0x5be0cd19u;
    c->total_len = 0;
    c->buffer_len = 0;
}

static void sha256_compress(sha256_ctx* c, const uint8_t* block) {
    uint32_t w[64];
    uint32_t a, b, cc, d, e, f, g, h;
    int i;

    for (i = 0; i < 16; i++) {
        w[i] = ((uint32_t)block[i * 4] << 24) |
               ((uint32_t)block[i * 4 + 1] << 16) |
               ((uint32_t)block[i * 4 + 2] << 8) |
               ((uint32_t)block[i * 4 + 3]);
    }
    for (i = 16; i < 64; i++) {
        uint32_t s0 = ROR(w[i - 15], 7) ^ ROR(w[i - 15], 18) ^ (w[i - 15] >> 3);
        uint32_t s1 = ROR(w[i - 2], 17) ^ ROR(w[i - 2], 19) ^ (w[i - 2] >> 10);
        w[i] = w[i - 16] + s0 + w[i - 7] + s1;
    }

    a = c->state[0]; b = c->state[1]; cc = c->state[2]; d = c->state[3];
    e = c->state[4]; f = c->state[5]; g = c->state[6]; h = c->state[7];

    for (i = 0; i < 64; i++) {
        uint32_t S1 = ROR(e, 6) ^ ROR(e, 11) ^ ROR(e, 25);
        uint32_t ch = (e & f) ^ (~e & g);
        uint32_t temp1 = h + S1 + ch + K256[i] + w[i];
        uint32_t S0 = ROR(a, 2) ^ ROR(a, 13) ^ ROR(a, 22);
        uint32_t maj = (a & b) ^ (a & cc) ^ (b & cc);
        uint32_t temp2 = S0 + maj;

        h = g; g = f; f = e; e = d + temp1;
        d = cc; cc = b; b = a; a = temp1 + temp2;
    }

    c->state[0] += a; c->state[1] += b; c->state[2] += cc; c->state[3] += d;
    c->state[4] += e; c->state[5] += f; c->state[6] += g; c->state[7] += h;
}

static void sha256_update(sha256_ctx* c, const uint8_t* data, size_t len) {
    c->total_len += len;

    if (c->buffer_len > 0) {
        size_t need = 64 - c->buffer_len;
        if (len >= need) {
            memcpy(c->buffer + c->buffer_len, data, need);
            sha256_compress(c, c->buffer);
            c->buffer_len = 0;
            data += need;
            len -= need;
        } else {
            memcpy(c->buffer + c->buffer_len, data, len);
            c->buffer_len += len;
            return;
        }
    }

    while (len >= 64) {
        sha256_compress(c, data);
        data += 64;
        len -= 64;
    }

    if (len > 0) {
        memcpy(c->buffer, data, len);
        c->buffer_len = len;
    }
}

static void sha256_final(sha256_ctx* c, uint8_t out[32]) {
    uint64_t bit_len = c->total_len * 8;
    uint8_t  padding = 0x80;
    int i;

    sha256_update(c, &padding, 1);

    /* 填充到 56 字节对齐 */
    uint8_t zero = 0;
    while (c->buffer_len != 56) {
        sha256_update(c, &zero, 1);
    }

    /* 追加 64 位大端长度 */
    uint8_t len_bytes[8];
    for (i = 0; i < 8; i++) {
        len_bytes[i] = (uint8_t)(bit_len >> (56 - i * 8));
    }
    sha256_update(c, len_bytes, 8);

    /* 输出 */
    for (i = 0; i < 8; i++) {
        out[i * 4]     = (uint8_t)(c->state[i] >> 24);
        out[i * 4 + 1] = (uint8_t)(c->state[i] >> 16);
        out[i * 4 + 2] = (uint8_t)(c->state[i] >> 8);
        out[i * 4 + 3] = (uint8_t)(c->state[i]);
    }
    memset(c, 0, sizeof(*c));
}

/* ============ HMAC-SHA256（RFC 2104）============ */

static void hmac_sha256(const uint8_t* key, size_t key_len,
                        const uint8_t* msg, size_t msg_len,
                        uint8_t out[32]) {
    uint8_t k_pad[64];
    uint8_t inner[64 + 32];   /* ipad 64 + 两次哈希缓冲 */
    uint8_t i_hash[32];
    uint8_t block[64];
    sha256_ctx ctx;
    size_t i;

    /* 密钥超长则先哈希 */
    if (key_len > 64) {
        sha256_init(&ctx);
        sha256_update(&ctx, key, key_len);
        sha256_final(&ctx, block);
        memcpy(k_pad, block, 32);
        for (i = 32; i < 64; i++) k_pad[i] = 0;
    } else {
        memset(k_pad, 0, 64);
        memcpy(k_pad, key, key_len);
    }

    /* 内层：H(K^ipad || msg) */
    sha256_init(&ctx);
    for (i = 0; i < 64; i++) inner[i] = k_pad[i] ^ 0x36;
    sha256_update(&ctx, inner, 64);
    sha256_update(&ctx, msg, msg_len);
    sha256_final(&ctx, i_hash);

    /* 外层：H(K^opad || inner_hash) */
    sha256_init(&ctx);
    for (i = 0; i < 64; i++) inner[i] = k_pad[i] ^ 0x5c;
    sha256_update(&ctx, inner, 64);
    sha256_update(&ctx, i_hash, 32);
    sha256_final(&ctx, out);
}

/* ============ PBKDF2-HMAC-SHA256（RFC 2898）============ */

static int pbkdf2_hmac_sha256(const uint8_t* pass, size_t pass_len,
                              const uint8_t* salt, size_t salt_len,
                              uint32_t iterations, uint8_t* out, size_t out_len) {
    uint8_t u[32];
    uint8_t t[32];
    uint8_t block_idx[4];
    uint32_t i, j;
    size_t k;

    if (iterations < 1 || out_len == 0) return 0;

    for (i = 1; out_len > 0; i++) {
        /* U1 = PRF(P, S || INT_32_BE(i)) */
        block_idx[0] = (uint8_t)(i >> 24);
        block_idx[1] = (uint8_t)(i >> 16);
        block_idx[2] = (uint8_t)(i >> 8);
        block_idx[3] = (uint8_t)(i);

        /* 构造盐+块号 的临时缓冲 */
        uint8_t* salt_block = (uint8_t*)malloc(salt_len + 4);
        if (!salt_block) return 0;
        memcpy(salt_block, salt, salt_len);
        memcpy(salt_block + salt_len, block_idx, 4);

        hmac_sha256(pass, pass_len, salt_block, salt_len + 4, u);
        free(salt_block);
        memcpy(t, u, 32);

        /* U2..Uc = PRF(P, U_{n-1}); T = U1 ^ U2 ^ ... ^ Uc */
        for (j = 1; j < iterations; j++) {
            hmac_sha256(pass, pass_len, u, 32, u);
            for (k = 0; k < 32; k++) t[k] ^= u[k];
        }

        /* 拷贝到输出 */
        size_t chunk = out_len < 32 ? out_len : 32;
        memcpy(out, t, chunk);
        out += chunk;
        out_len -= chunk;
    }
    return 1;
}

/* ============ constant-time 比较 ============ */

static int const_time_eq(const uint8_t* a, const uint8_t* b, size_t len) {
    uint8_t diff = 0;
    size_t i;
    for (i = 0; i < len; i++) {
        diff |= a[i] ^ b[i];
    }
    return diff == 0;
}

/* ============ JNI 工具 ============ */

static void secure_wipe(void* ptr, size_t len) {
    if (ptr && len > 0) {
        volatile uint8_t* p = (volatile uint8_t*)ptr;
        while (len--) *p++ = 0;
    }
}

static uint8_t* jbyte_to_uchar(JNIEnv* env, jbyteArray arr, jsize* out_len) {
    if (!arr) { *out_len = 0; return NULL; }
    *out_len = (*env)->GetArrayLength(env, arr);
    uint8_t* buf = (uint8_t*)malloc(*out_len > 0 ? *out_len : 1);
    if (!buf) return NULL;
    if (*out_len > 0) {
        (*env)->GetByteArrayRegion(env, arr, 0, *out_len, (jbyte*)buf);
    }
    return buf;
}

static jbyteArray uchar_to_jbyte(JNIEnv* env, const uint8_t* buf, jsize len) {
    jbyteArray result = (*env)->NewByteArray(env, len);
    if (!result) return NULL;
    (*env)->SetByteArrayRegion(env, result, 0, len, (const jbyte*)buf);
    return result;
}

/* ============ JNI 导出 ============ */

/*
 * Java_com_zaa_cryptdroid_security_NativeCrypto_nativeDeriveKey
 * 主密码派生：PBKDF2-HMAC-SHA256(主密码, 盐, 迭代次数, 密钥长度)
 */
JNIEXPORT jbyteArray JNICALL
Java_com_zaa_cryptdroid_security_NativeCrypto_nativeDeriveKey(
        JNIEnv* env, jobject thiz,
        jbyteArray jPassword, jbyteArray jSalt,
        jint iterations, jint keyLen) {

    jsize pass_len, salt_len;
    uint8_t* pass = jbyte_to_uchar(env, jPassword, &pass_len);
    uint8_t* salt = jbyte_to_uchar(env, jSalt, &salt_len);

    if ((jPassword && !pass) || (jSalt && !salt)) {
        free(pass); free(salt);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"), "alloc failed");
        return NULL;
    }

    if (iterations < 1 || iterations > 100000000) iterations = 10000;
    if (keyLen < 1 || keyLen > 1024) keyLen = 32;

    uint8_t* out = (uint8_t*)malloc(keyLen);
    if (!out) {
        free(pass); free(salt);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"), "malloc failed");
        return NULL;
    }

    int ok = pbkdf2_hmac_sha256(pass, pass_len, salt, salt_len, iterations, out, keyLen);

    secure_wipe(pass, pass_len);
    secure_wipe(salt, salt_len);
    free(pass);
    free(salt);

    if (!ok) {
        secure_wipe(out, keyLen);
        free(out);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/security/GeneralSecurityException"), "PBKDF2 failed");
        return NULL;
    }

    jbyteArray result = uchar_to_jbyte(env, out, keyLen);
    secure_wipe(out, keyLen);
    free(out);
    return result;
}

/*
 * Java_com_zaa_cryptdroid_security_NativeCrypto_nativeVerifyPassword
 * 主密码校验：派生后 constant-time 比较指纹
 */
JNIEXPORT jboolean JNICALL
Java_com_zaa_cryptdroid_security_NativeCrypto_nativeVerifyPassword(
        JNIEnv* env, jobject thiz,
        jbyteArray jPassword, jbyteArray jSalt,
        jint iterations, jbyteArray jExpected) {

    jsize pass_len, salt_len, expected_len;
    uint8_t* pass = jbyte_to_uchar(env, jPassword, &pass_len);
    uint8_t* salt = jbyte_to_uchar(env, jSalt, &salt_len);
    uint8_t* expected = jbyte_to_uchar(env, jExpected, &expected_len);

    if (iterations < 1 || iterations > 100000000) iterations = 10000;
    if (expected_len != 32) {
        free(pass); free(salt); free(expected);
        return JNI_FALSE;
    }

    uint8_t derived[32];
    int ok = pbkdf2_hmac_sha256(pass, pass_len, salt, salt_len, iterations, derived, 32);

    secure_wipe(pass, pass_len);
    secure_wipe(salt, salt_len);
    free(pass);
    free(salt);

    if (!ok) {
        free(expected);
        return JNI_FALSE;
    }

    jboolean match = const_time_eq(derived, expected, 32) ? JNI_TRUE : JNI_FALSE;

    secure_wipe(derived, 32);
    secure_wipe(expected, expected_len);
    free(expected);
    return match;
}
