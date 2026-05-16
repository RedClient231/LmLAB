cmake_minimum_required(VERSION 3.22.1)
project("voidguardian")

add_library(voidguardian SHARED
        src/gg_injector.cpp
        src/memory_mapper.cpp
        src/ptrace_hook.cpp)

find_library(log-lib log)

target_link_libraries(voidguardian
        android
        ${log-lib})
