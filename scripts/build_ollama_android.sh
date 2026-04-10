#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

NDK_VERSION="${ANDROID_NDK_VERSION:-28.2.13676358}"
NDK_HOME="${ANDROID_NDK_HOME:-$HOME/Library/Android/sdk/ndk/$NDK_VERSION}"
TOOLCHAIN="$NDK_HOME/toolchains/llvm/prebuilt/darwin-x86_64"
MIN_SDK=30

JNI_LIBS_DIR="$REPO_ROOT/app/androidApp/src/main/jniLibs"

build_arch() {
    local ARCH=$1
    local GO_ARCH=$2
    local CLANG_PREFIX=$3

    local CC="$TOOLCHAIN/bin/${CLANG_PREFIX}${MIN_SDK}-clang"
    local CXX="$TOOLCHAIN/bin/${CLANG_PREFIX}${MIN_SDK}-clang++"
    local OUT="$JNI_LIBS_DIR/$ARCH/libollama.so"

    echo "Building ollama for $ARCH..."

    mkdir -p "$JNI_LIBS_DIR/$ARCH"

    GOOS=android \
    GOARCH=$GO_ARCH \
    CGO_ENABLED=1 \
    CC="$CC" \
    CXX="$CXX" \
    go build \
        -ldflags="-s -w" \
        -o "$OUT" \
        "$REPO_ROOT/ollama"

    local SYSROOT_LIB="$TOOLCHAIN/sysroot/usr/lib/${CLANG_PREFIX}"
    cp "$SYSROOT_LIB/libc++_shared.so" "$JNI_LIBS_DIR/$ARCH/libc++_shared.so"
    echo "Built: $OUT ($(du -sh "$OUT" | cut -f1))"
}

cd "$REPO_ROOT/ollama"

build_arch "arm64-v8a"   "arm64"  "aarch64-linux-android"
build_arch "x86_64"      "amd64"  "x86_64-linux-android"

echo "Done."
