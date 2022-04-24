cmake_minimum_required(VERSION 3.21)
project(termio_native C)

set(CMAKE_C_STANDARD 99)

include_directories(src)

add_library(termio SHARED
        src/jni.h
        src/jni_md.h
        src/termio_windows.c)
