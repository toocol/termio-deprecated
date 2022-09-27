cmake_minimum_required (VERSION 3.8)

project ("nativenode")

if (WIN32)
    add_definitions(/utf-8)
endif()

include_directories("java/")
include_directories("src/")
include_directories("../jni/")
include_directories("D:/download/boost_1_80_0/")

link_directories("D:/download/boost_1_80_0/stage/lib")

set(HEADERS 
	java/com_toocol_termio_platform_nativefx_NativeBinding.h
	src/jnitypeconverter.h
	src/sharedmemory.h
)
set(SOURCES
	src/jnitypeconverter.cpp
	src/nativenode.cpp
)

add_library(nativenode SHARED
	${SOURCES}
)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET nativenode PROPERTY CXX_STANDARD 20)
endif()

INSTALL(FILES 
	${HEADERS}
	DESTINATION
	include
)

INSTALL(TARGETS nativenode LIBRARY DESTINATION lib)
