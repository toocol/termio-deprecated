cmake_minimum_required(VERSION 3.5)

project(emulator VERSION 0.1 LANGUAGES CXX)

set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(TERMINAL_EMULATOR_LIBRARY_NAME emulator)

if(NOT CMAKE_BUILD_TYPE)
set(CMAKE_BUILD_TYPE Release)
endif()
#set(CMAKE_CXX_FLAGS_RELEASE -Ofast)
#set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} -O3  -Wall")
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3  -Wall")

find_package(QT NAMES Qt6 Qt5 REQUIRED COMPONENTS Widgets)
find_package(Qt${QT_VERSION_MAJOR} REQUIRED COMPONENTS Widgets)
find_package(Qt${QT_VERSION_MAJOR} REQUIRED COMPONENTS Core5Compat)

include_directories("src/headers/")
include_directories("src/cpp/")
include_directories("../native/native-adapter/src/")
include_directories("D:/download/boost_1_80_0/")
include_directories("C:/Windows/System32/include")

link_directories("D:/download/boost_1_80_0/stage/lib")
link_directories("C:/Windows/System32/lib")
link_directories("C:/Windows/System32/lib/sys")
link_directories("C:/Windows/System32/bin")

set(HEADERS
        src/headers/nativers_server.hpp
        src/headers/args.hxx
        src/headers/character.h
        src/headers/character_color.h
        src/headers/wcwidth.h
        src/headers/history.h
        src/headers/block_array.h
        src/headers/screen.h
        src/headers/screen_window.h
        src/headers/terminal_character_decoder.h
        src/headers/terminal_emulator.h
        src/headers/terminal_view.h
        src/headers/emulation.h
        src/headers/filter.h
        src/headers/keyboard_translator.h
        src/headers/tools.h
        src/headers/vt102emulation.h
        src/headers/kpty.h
        src/headers/kpty_p.h
        src/headers/kprocess.h
        src/headers/kpty_device.h
        src/headers/kpty_process.h
        src/headers/pty.h
        src/headers/virtual_pty.h
        src/headers/conpty.h
        src/headers/session.h
        src/headers/shell_command.h
        src/headers/tabs_bar.h
        src/headers/tab.h
        src/headers/tab_button.h
        src/headers/transmit_signals.h
)
set(SOURCES
        src/cpp/main.cpp
        src/cpp/wcwidth.cpp
        src/cpp/history.cpp
        src/cpp/block_array.cpp
        src/cpp/screen.cpp
        src/cpp/screen_window.cpp
        src/cpp/terminal_character_decoder.cpp
        src/cpp/terminal_emulator.cpp
        src/cpp/terminal_view.cpp
        src/cpp/emulation.cpp
        src/cpp/filter.cpp
        src/cpp/keyboard_translator.cpp
        src/cpp/tools.cpp
        src/cpp/vt102emulation.cpp
        src/cpp/kpty.cpp
        src/cpp/kprocess.cpp
        src/cpp/kpty_device.cpp
        src/cpp/kpty_process.cpp
        src/cpp/pty.cpp
        src/cpp/conpty.cpp
        src/cpp/session.cpp
        src/cpp/shell_command.cpp
        src/cpp/tabs_bar.cpp
        src/cpp/tab.cpp
        src/cpp/tab_button.cpp
        src/cpp/transmit_signals.cpp
)

if (WIN32)
    add_definitions(/utf-8)
endif()

# dirs start
#set(KB_LAYOUT_DIR "${CMAKE_INSTALL_FULL_DATADIR}/${TERMINAL_EMULATOR_LIBRARY_NAME}/kb-layouts")
set(KB_LAYOUT_DIR "${CMAKE_INSTALL_LIBDIR}/kb-layouts")
message(STATUS "Keyboard layouts will be installed in: ${KB_LAYOUT_DIR}")

#set(COLORSCHEMES_DIR "${CMAKE_INSTALL_FULL_DATADIR}/${TERMINAL_EMULATOR_LIBRARY_NAME}/color-schemes")
set(COLORSCHEMES_DIR "${CMAKE_INSTALL_LIBDIR}/color-schemes")
message(STATUS "Color schemes will be installed in: ${COLORSCHEMES_DIR}" )

set(QSS_DIR "${CMAKE_INSTALL_LIBDIR}/qss")
message(STATUS "Qss style sheet will be installed in: ${COLORSCHEMES_DIR}" )
# dirs end

if(${QT_VERSION_MAJOR} GREATER_EQUAL 6)
    qt_add_executable(emulator
        MANUAL_FINALIZATION
        ${HEADERS}
        ${SOURCES}
    )
# Define target properties for Android with Qt 6 as:
#    set_property(TARGET emulator APPEND PROPERTY QT_ANDROID_PACKAGE_SOURCE_DIR
#                 ${CMAKE_CURRENT_SOURCE_DIR}/android)
# For more information, see https://doc.qt.io/qt-6/qt-add-executable.html#target-creation
else()
    if(ANDROID)
        add_library(emulator SHARED
            ${HEADERS}
            ${SOURCES}
        )
# Define properties for Android with Qt 5 after find_package() calls as:
#    set(ANDROID_PACKAGE_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/android")
    else()
        add_executable(emulator
            ${HEADERS}
            ${SOURCES}
        )
    endif()
endif()

target_link_libraries(emulator PRIVATE Qt${QT_VERSION_MAJOR}::Widgets Qt${QT_VERSION_MAJOR}::Core5Compat utf8proc mman winconpty)

set_target_properties(emulator PROPERTIES
    MACOSX_BUNDLE_GUI_IDENTIFIER my.example.com
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
    COMPILE_FLAGS "/EHsc"
)

target_compile_definitions(${TERMINAL_EMULATOR_LIBRARY_NAME}
    PRIVATE
        "KB_LAYOUT_DIR=\"${KB_LAYOUT_DIR}\""
        "COLORSCHEMES_DIR=\"${COLORSCHEMES_DIR}\""
        "QSS_DIR=\"${QSS_DIR}\""
        "HAVE_UTF8PROC"
#        "TRANSLATIONS_DIR=\"${TRANSLATIONS_DIR}\""
#        "HAVE_POSIX_OPENPT"
#        "HAVE_SYS_TIME_H"
)

string(APPEND PKG_CONFIG_REQ ", libutf8proc")

# keyboard layouts
install(DIRECTORY
    lib/kb-layouts/
    DESTINATION "${KB_LAYOUT_DIR}"
    COMPONENT Runtime
    FILES_MATCHING PATTERN "*.keytab"
)

# color schemes
install(DIRECTORY
    lib/color-schemes/
    DESTINATION "${COLORSCHEMES_DIR}"
    COMPONENT Runtime
    FILES_MATCHING PATTERN "*.*schem*"
)

# qss style sheet
install(DIRECTORY
    lib/color-schemes/
    DESTINATION "${QSS_DIR}"
    COMPONENT Runtime
    FILES_MATCHING PATTERN "*.qss"
)

install(TARGETS emulator
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR})

if(QT_VERSION_MAJOR EQUAL 6)
    qt_finalize_executable(emulator)
endif()
