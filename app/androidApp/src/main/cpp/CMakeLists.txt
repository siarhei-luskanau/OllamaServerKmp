cmake_minimum_required(VERSION 3.21)

project(OllamaAndroid C CXX)

set(OLLAMA_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../ollama")

include(CheckLanguage)
include(GNUInstallDirs)
include(FetchContent)
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

# ggml's C/C++ source is no longer vendored inside the ollama submodule -
# ollama/llama/server/CMakeLists.txt fetches it via FetchContent at the tag
# pinned by LLAMA_CPP_VERSION. Mirror that here so the ggml version used for
# this build always matches the pinned ollama submodule.
file(READ "${OLLAMA_ROOT}/LLAMA_CPP_VERSION" LLAMA_CPP_GIT_TAG)
string(STRIP "${LLAMA_CPP_GIT_TAG}" LLAMA_CPP_GIT_TAG)

# Share one checkout across all ABI/build-type configures instead of letting
# each cmake process (AGP runs them in parallel) clone llama.cpp separately.
set(FETCHCONTENT_BASE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../build/_deps" CACHE PATH "" FORCE)
file(MAKE_DIRECTORY "${FETCHCONTENT_BASE_DIR}")

FetchContent_Declare(
    llama_cpp
    GIT_REPOSITORY "https://github.com/ggml-org/llama.cpp.git"
    GIT_TAG ${LLAMA_CPP_GIT_TAG}
    GIT_SHALLOW TRUE
)

# Serialize the populate step across the parallel cmake processes so they
# don't race on the same FETCHCONTENT_BASE_DIR subbuild directory.
file(LOCK "${FETCHCONTENT_BASE_DIR}/llama_cpp.lock")
FetchContent_GetProperties(llama_cpp)
if(NOT llama_cpp_POPULATED)
    FetchContent_Populate(llama_cpp)
endif()
file(LOCK "${FETCHCONTENT_BASE_DIR}/llama_cpp.lock" RELEASE)

set(GGML_ROOT "${llama_cpp_SOURCE_DIR}/ggml")

set(GGML_INCLUDE_DIRS
    ${GGML_ROOT}/src
    ${GGML_ROOT}/src/include
    ${GGML_ROOT}/src/ggml-cpu
    ${GGML_ROOT}/src/ggml-cpu/amx
)

add_subdirectory(${GGML_ROOT}/src ggml-src)

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
