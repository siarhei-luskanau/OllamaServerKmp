cmake_minimum_required(VERSION 3.21)

project(OllamaAndroid C CXX)

set(OLLAMA_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../ollama")

include(CheckLanguage)
include(GNUInstallDirs)
find_package(Threads REQUIRED)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS ON)

set(BUILD_SHARED_LIBS ON)
set(GGML_CPU ON)
set(GGML_SHARED ON)
set(GGML_BUILD ON)
set(GGML_BACKEND_DL ON)
set(GGML_BACKEND_SHARED ON)
set(GGML_SCHED_MAX_COPIES 4)
set(GGML_LLAMAFILE OFF)
set(GGML_CCACHE OFF)

set(GGML_VULKAN OFF)
set(GGML_CUDA OFF)
set(GGML_METAL OFF)
set(GGML_HIP OFF)
set(MLX_ENGINE OFF)
set(GGML_CPU_ALL_VARIANTS OFF)

set(GGML_VERSION_MAJOR 0)
set(GGML_VERSION_MINOR 0)
set(GGML_VERSION_PATCH 0)
set(GGML_VERSION "${GGML_VERSION_MAJOR}.${GGML_VERSION_MINOR}.${GGML_VERSION_PATCH}")

add_compile_definitions(NDEBUG GGML_VERSION=0x0 GGML_COMMIT=0x0)

set(GGML_INCLUDE_DIRS
    ${OLLAMA_ROOT}/ml/backend/ggml/ggml/src
    ${OLLAMA_ROOT}/ml/backend/ggml/ggml/src/include
    ${OLLAMA_ROOT}/ml/backend/ggml/ggml/src/ggml-cpu
    ${OLLAMA_ROOT}/ml/backend/ggml/ggml/src/ggml-cpu/amx
)

add_subdirectory(${OLLAMA_ROOT}/ml/backend/ggml/ggml/src ggml-src)

set_property(TARGET ggml PROPERTY EXCLUDE_FROM_ALL TRUE)

get_target_property(CPU_VARIANTS ggml-cpu MANUALLY_ADDED_DEPENDENCIES)
if(NOT CPU_VARIANTS)
    set(CPU_VARIANTS "ggml-cpu")
endif()

target_include_directories(ggml-base PRIVATE ${GGML_INCLUDE_DIRS})
foreach(variant ${CPU_VARIANTS})
    if(TARGET ${variant})
        target_include_directories(${variant} PRIVATE ${GGML_INCLUDE_DIRS})
    endif()
endforeach()
