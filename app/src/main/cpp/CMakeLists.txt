# CMake 构建配置：编译 crypto_native.so
# 职责：把"主密码派生 + 密码本加解密"下沉到 native 层，提高逆向破解难度。
cmake_minimum_required(VERSION 3.22.1)

project(crypto_native)

# 生成原生库（无 STL 依赖，纯 C，控制体积）
add_library(crypto_native SHARED crypto_native.c)

# 查找系统 BoringSSL 库（Android 系统自带 libcrypto，动态链接不打包进 APK）
# find_library 在 NDK sysroot 中定位 crypto 库，链接后运行时由系统提供
find_library(CRYPTO_LIB crypto)

# 链接：系统 BoringSSL（crypto）+ Android 平台库（android/log）
target_link_libraries(crypto_native ${CRYPTO_LIB} android log)

# 优化等级：release 体积优先（-Os），符号本地化防导出
target_compile_options(crypto_native PRIVATE -Os -fvisibility=hidden)
