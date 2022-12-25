cmake_minimum_required (VERSION 3.8)

project ("native-system")

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
include_directories("C:/Windows/System32/include")
include_directories("C:/Windows/System32/include/sys")

link_directories("C:/Windows/System32/lib")

set(HEADERS 
	src/native_system.h
)
set(SOURCES
	src/native_system.cpp
)

add_library(mman STATIC IMPORTED)
add_library(utf8proc STATIC IMPORTED)
set_property(TARGET mman PROPERTY IMPORTED_LOCATION C:/Windows/System32/lib/sys/mman.lib)
set_property(TARGET utf8proc PROPERTY IMPORTED_LOCATION C:/Windows/System32/lib/utf8proc.lib)
add_library(native-system STATIC
	${SOURCES}
)

target_link_libraries(native-system mman utf8proc)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET native-system PROPERTY CXX_STANDARD 20)
  set_property(TARGET native-system PROPERTY COMPILE_FLAGS "/EHsc")
endif()

INSTALL(FILES 
	${HEADERS}
	DESTINATION
	include
)

INSTALL(TARGETS native-system LIBRARY DESTINATION lib)
