cmake_minimum_required(VERSION 3.24)

project(windows_hello LANGUAGES CXX)

add_library(windows_hello SHARED
    src/windows_hello.cpp
)

target_compile_features(windows_hello PRIVATE cxx_std_20)

target_include_directories(windows_hello
    PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include
)

target_compile_definitions(windows_hello PRIVATE
    WIN32_LEAN_AND_MEAN
    NOMINMAX
    WINDOWS_HELLO_EXPORTS
)

target_link_libraries(windows_hello PRIVATE
    runtimeobject
    user32
)

set_target_properties(windows_hello PROPERTIES
    OUTPUT_NAME windows_hello
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
    ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
    RUNTIME_OUTPUT_DIRECTORY_RELEASE ${CMAKE_BINARY_DIR}/bin/Release
    LIBRARY_OUTPUT_DIRECTORY_RELEASE ${CMAKE_BINARY_DIR}/bin/Release
    ARCHIVE_OUTPUT_DIRECTORY_RELEASE ${CMAKE_BINARY_DIR}/bin/Release
)
