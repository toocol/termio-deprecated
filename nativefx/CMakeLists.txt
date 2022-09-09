cmake_minimum_required(VERSION 3.5)

project(nativefx VERSION 0.1 LANGUAGES CXX)

set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(TERMINAL_EMULATOR_LIBRARY_NAME nativefx)

find_package(QT NAMES Qt6 Qt5 REQUIRED COMPONENTS Widgets)
find_package(Qt${QT_VERSION_MAJOR} REQUIRED COMPONENTS Widgets)
find_package(Qt${QT_VERSION_MAJOR} REQUIRED COMPONENTS Core5Compat)

include_directories("../native/src/")
include_directories("src/headers/")
include_directories("src/cpp/")
include_directories("D:/download/boost_1_80_0/")
include_directories("C:/Windows/System32/include")

link_directories("D:/download/boost_1_80_0/stage/lib")
link_directories("C:/Windows/System32/lib")

set(PROJECT_SOURCES
        src/headers/jnitypeconverter.h
        src/headers/shared_memory.h
        src/headers/character.h
        src/headers/charactercolor.h
        src/headers/wcwidth.h
        src/headers/history.h
        src/headers/blockarray.h
        src/headers/screen.h
        src/headers/screenwindow.h
        src/headers/terminalcharacterdecoder.h
        src/headers/terminalemulator.h
        src/headers/terminalview.h
        src/headers/emulation.h
        src/headers/filter.h
        src/headers/keyboardtranslator.h
        src/headers/tools.h
        src/headers/vt102emulation.h
        src/cpp/main.cpp
        src/cpp/jnitypeconverter.cpp
        src/cpp/wcwidth.cpp
        src/cpp/history.cpp
        src/cpp/blockarray.cpp
        src/cpp/screen.cpp
        src/cpp/screenwindow.cpp
        src/cpp/terminalcharacterdecoder.cpp
        src/cpp/terminalemulator.cpp
        src/cpp/terminalview.cpp
        src/cpp/emulation.cpp
        src/cpp/filter.cpp
        src/cpp/keyboardtranslator.cpp
        src/cpp/tools.cpp
        src/cpp/vt102emulation.cpp
)

# dirs start
set(KB_LAYOUT_DIR "${CMAKE_INSTALL_FULL_DATADIR}/${TERMINAL_EMULATOR_LIBRARY_NAME}/kb-layouts")
message(STATUS "Keyboard layouts will be installed in: ${KB_LAYOUT_DIR}")

set(COLORSCHEMES_DIR "${CMAKE_INSTALL_FULL_DATADIR}/${TERMINAL_EMULATOR_LIBRARY_NAME}/color-schemes")
message(STATUS "Color schemes will be installed in: ${COLORSCHEMES_DIR}" )
# dirs end

if(${QT_VERSION_MAJOR} GREATER_EQUAL 6)
    qt_add_executable(nativefx
        MANUAL_FINALIZATION
        ${PROJECT_SOURCES}
    )
# Define target properties for Android with Qt 6 as:
#    set_property(TARGET nativefx APPEND PROPERTY QT_ANDROID_PACKAGE_SOURCE_DIR
#                 ${CMAKE_CURRENT_SOURCE_DIR}/android)
# For more information, see https://doc.qt.io/qt-6/qt-add-executable.html#target-creation
else()
    if(ANDROID)
        add_library(nativefx SHARED
            ${PROJECT_SOURCES}
        )
# Define properties for Android with Qt 5 after find_package() calls as:
#    set(ANDROID_PACKAGE_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/android")
    else()
        add_executable(nativefx
            ${PROJECT_SOURCES}
        )
    endif()
endif()

target_link_libraries(nativefx PRIVATE Qt${QT_VERSION_MAJOR}::Widgets -lmman Qt${QT_VERSION_MAJOR}::Core5Compat)

set_target_properties(nativefx PROPERTIES
    MACOSX_BUNDLE_GUI_IDENTIFIER my.example.com
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
)

target_compile_definitions(${TERMINAL_EMULATOR_LIBRARY_NAME}
    PRIVATE
        "KB_LAYOUT_DIR=\"${KB_LAYOUT_DIR}\""
        "COLORSCHEMES_DIR=\"${COLORSCHEMES_DIR}\""
#        "TRANSLATIONS_DIR=\"${TRANSLATIONS_DIR}\""
#        "HAVE_POSIX_OPENPT"
#        "HAVE_SYS_TIME_H"
)

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

install(TARGETS nativefx
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR})

if(QT_VERSION_MAJOR EQUAL 6)
    qt_finalize_executable(nativefx)
endif()
