cmake_minimum_required (VERSION 3.8)

project ("native-fontconfig")

if (WIN32)
    add_definitions(/utf-8)
endif()

if(NOT CMAKE_BUILD_TYPE)
set(CMAKE_BUILD_TYPE Release)
endif()
set(CMAKE_CXX_FLAGS_RELEASE -Ofast)
set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} -O3  -Wall")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3  -Wall")

add_definitions(-DUNICODE)

include_directories("src/")

set(HEADERS 
    src/fontconfig.h
)
set(SOURCES
    src/fontconfig.cpp
)

add_library(native-fontconfig SHARED
	${SOURCES}
)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET native-fontconfig PROPERTY CXX_STANDARD 20)
endif()

INSTALL(FILES 
	${HEADERS}
	DESTINATION
	include
)

INSTALL(TARGETS native-fontconfig LIBRARY DESTINATION lib)
