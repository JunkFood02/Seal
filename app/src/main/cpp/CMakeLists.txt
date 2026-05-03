cmake_minimum_required(VERSION 3.18.1)

# Project name
project("seal_quickjs")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_STANDARD 11)

# QuickJS configuration
set(QUICKJS_VERSION "2025-09-13")
set(QUICKJS_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/quickjs)

# Include QuickJS source files
file(GLOB QUICKJS_SOURCES
    "${QUICKJS_SOURCE_DIR}/*.c"
    "${QUICKJS_SOURCE_DIR}/libunicode/*.c"
    "${QUICKJS_SOURCE_DIR}/libregexp/*.c"
)

# Exclude qjsc and qjs from build (we only need the library)
list(FILTER QUICKJS_SOURCES EXCLUDE REGEX ".*/qjsc$")
list(FILTER QUICKJS_SOURCES EXCLUDE REGEX ".*/qjs$")
list(FILTER QUICKJS_SOURCES EXCLUDE REGEX ".*/repl$")
list(FILTER QUICKJS_SOURCES EXCLUDE REGEX ".*/qjscalc$")

# Create QuickJS static library
add_library(quickjs STATIC ${QUICKJS_SOURCES})

# Set compile definitions for QuickJS
target_compile_definitions(quickjs PRIVATE
    CONFIG_VERSION="${QUICKJS_VERSION}"
    _GNU_SOURCE
)

# Include directories
target_include_directories(quickjs PUBLIC
    ${QUICKJS_SOURCE_DIR}
    ${QUICKJS_SOURCE_DIR}/libunicode
    ${QUICKJS_SOURCE_DIR}/libregexp
)

# Create JNI bridge library
add_library(quickjs_jni SHARED
    quickjs_jni.cpp
    quickjs_android_runtime.cpp
)

# Link libraries
target_link_libraries(quickjs_jni
    quickjs
    log
    android
)

# Include directories for JNI
target_include_directories(quickjs_jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${QUICKJS_SOURCE_DIR}
)

# Set compiler flags
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    target_compile_options(quickjs PRIVATE -O3 -ffast-math)
    target_compile_options(quickjs_jni PRIVATE -O3 -ffast-math)
else()
    target_compile_options(quickjs PRIVATE -O0 -g)
    target_compile_options(quickjs_jni PRIVATE -O0 -g)
endif()

# Position independent code for static library
set_target_properties(quickjs PROPERTIES POSITION_INDEPENDENT_CODE ON)
