cmake_minimum_required (VERSION 3.8)

project ("native-adapter")

if (WIN32)
    add_definitions(/utf-8)
endif()

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

add_library(native-adapter SHARED
	${SOURCES}
)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET native-adapter PROPERTY CXX_STANDARD 20)
endif()

INSTALL(FILES 
	${HEADERS}
	DESTINATION
	include
)

INSTALL(TARGETS native-adapter LIBRARY DESTINATION lib)
