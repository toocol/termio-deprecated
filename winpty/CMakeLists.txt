cmake_minimum_required (VERSION 3.8)

project ("winpty")

set(HEADERS 
	src/winpty.h
)
set(SOURCES
	src/winpty.cpp
)

add_library(winpty SHARED
	${HEADERS}
	${SOURCES}
)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET winpty PROPERTY CXX_STANDARD 20)
endif()

INSTALL(FILES 
	${HEADERS}
	DESTINATION
	include
)

INSTALL(TARGETS winpty LIBRARY DESTINATION lib)
