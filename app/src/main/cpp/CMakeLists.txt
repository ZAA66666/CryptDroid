# CMake 构建配置：编译 crypto_native.so
# 职责：主密码派生（PBKDF2-HMAC-SHA256）+ 校验 下沉到 native 层，提高逆向破解难度。
# 说明：纯 C 自实现（SHA-256/HMAC/PBKDF2），不依赖系统加密库——
#      NDK r26+ 已移除 BoringSSL 公共头文件，外部依赖会破坏构建。
cmake_minimum_required(VERSION 3.22.1)

project(crypto_native)

# 生成原生库（纯 C，无 STL 依赖，控制体积）
add_library(crypto_native SHARED crypto_native.c)

# 仅链接 Android 平台库（log 用于 JNI 日志，实际代码未用可保持链接）
target_link_libraries(crypto_native android log)

# 优化等级：release 体积优先（-Os），符号本地化防导出
target_compile_options(crypto_native PRIVATE -Os -fvisibility=hidden)
