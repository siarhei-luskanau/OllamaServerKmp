#!/bin/bash
set -euo pipefail
# Produces: build/llama.xcframework
# Requires: Xcode, CMake, ios.toolchain.cmake from https://github.com/leetal/ios-cmake

cmake -B build/ios-arm64 -G Xcode \
  -DCMAKE_TOOLCHAIN_FILE=cmake/ios.toolchain.cmake \
  -DPLATFORM=OS64 \
  -DLLAMA_METAL=ON \
  -DLLAMA_BUILD_SERVER=OFF \
  -DLLAMA_BUILD_TESTS=OFF \
  -DCMAKE_BUILD_TYPE=Release

cmake --build build/ios-arm64 --config Release -- -arch arm64

cmake -B build/ios-sim -G Xcode \
  -DCMAKE_TOOLCHAIN_FILE=cmake/ios.toolchain.cmake \
  -DPLATFORM=SIMULATORARM64 \
  -DLLAMA_METAL=ON \
  -DLLAMA_BUILD_SERVER=OFF \
  -DLLAMA_BUILD_TESTS=OFF

cmake --build build/ios-sim --config Release

xcodebuild -create-xcframework \
  -library build/ios-arm64/src/Release-iphoneos/libllama.a \
  -headers ollama/llama.cpp/include \
  -library build/ios-sim/src/Release-iphonesimulator/libllama.a \
  -headers ollama/llama.cpp/include \
  -output build/llama.xcframework
