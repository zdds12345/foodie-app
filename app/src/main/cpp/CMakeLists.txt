cmake_minimum_required(VERSION 3.22.1)
project("ademo")

add_library(
        ademo
        SHARED
        native-lib.cpp)

target_link_libraries(
        ademo
        android
        log)
