cmake_minimum_required (VERSION 3.8)

project ("native-adapter")

if (WIN32)
    add_definitions(/utf-8)
endif()

if(NOT CMAKE_BUILD_TYPE)
set(CMAKE_BUILD_TYPE Release)
endif()
set(CMAKE_CXX_FLAGS_RELEASE -Ofast)
set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} -O3  -Wall")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3  -Wall")

include_directories("src/")
include_directories("D:/download/boost_1_80_0/")

link_directories("D:/download/boost_1_80_0/stage/lib")

set(HEADERS 
	src/native_adapter.h
	src/shared_memory.h
)
set(SOURCES
	src/native_adapter.cpp
)

add_library(native-adapter STATIC
	${SOURCES}
)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET native-adapter PROPERTY CXX_STANDARD 20)
  set_property(TARGET native-adapter PROPERTY COMPILE_FLAGS "/EHsc")
endif()

INSTALL(FILES 
	${HEADERS}
	DESTINATION
	include
)

INSTALL(TARGETS native-adapter LIBRARY DESTINATION lib)
