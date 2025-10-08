cmake_minimum_required(VERSION 3.18.1)

project("edgedetection")

# Set C++ standard
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Set OpenCV path (we'll set this in the build.gradle file)
set(OpenCV_DIR ${CMAKE_CURRENT_BINARY_DIR}/opencv/sdk/native/jni)

# Find OpenCV package
find_package(OpenCV REQUIRED)

# Include directories
include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${OpenCV_INCLUDE_DIRS})

# Add library
add_library(
    edgedetection
    SHARED
    native-lib.cpp
    ImageProcessor.cpp
)

# Find and link libraries
find_library(
    log-lib
    log
)

find_library(
    jnigraphics-lib
    jnigraphics
)

target_link_libraries(
    edgedetection
    ${log-lib}
    ${jnigraphics-lib}
    ${OpenCV_LIBS}
)