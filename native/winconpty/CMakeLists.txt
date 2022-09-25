cmake_minimum_required (VERSION 3.8)

project ("winconpty")

set(HEADERS 
	src/winconpty.h
)
set(SOURCES
	src/winconpty.cpp
)

add_library(winconpty SHARED
	${HEADERS}
	${SOURCES}
)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET winconpty PROPERTY CXX_STANDARD 20)
endif()

INSTALL(FILES 
	${HEADERS}
	DESTINATION
	include
)

INSTALL(TARGETS winconpty LIBRARY DESTINATION lib)
