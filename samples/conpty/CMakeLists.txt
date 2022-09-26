cmake_minimum_required (VERSION 3.8)

project ("conpty")

set(HEADERS
    src/conpty.h
)
set(SOURCES
    src/conpty.cpp
)

add_executable (conpty
    ${HEADERS}
    ${SOURCES}
)

add_definitions(-DUNICODE)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET conpty PROPERTY CXX_STANDARD 20)
endif()