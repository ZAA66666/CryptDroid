# CMake 构建配置：编译 crypto_native.so
# 职责：把"主密码派生 + 密码本加解密"下沉到 native 层，提高逆向破解难度。
cmake_minimum_required(VERSION 3.22.1)

project(crypto_native)

# 生成原生库（无 STL 依赖，纯 C，控制体积）
add_library(crypto_native SHARED crypto_native.c)

# 链接系统 BoringSSL（Android 系统自带 libcrypto，动态链接不打包进 APK）
# 注意：不要用 find_library(CRYPTO_LIB crypto)——NDK 27 起 find_library 定位不到
#      系统库；直接写库名 crypto，链接器会自动从 NDK sysroot 解析出 libcrypto.so。
target_link_libraries(crypto_native crypto android log)

# 优化等级：release 体积优先（-Os），符号本地化防导出
target_compile_options(crypto_native PRIVATE -Os -fvisibility=hidden)
