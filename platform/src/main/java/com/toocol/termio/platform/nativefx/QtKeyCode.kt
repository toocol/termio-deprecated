package com.toocol.termio.platform.nativefx

import javafx.scene.input.KeyCode

/**
 * Comparison of keyboard code between Qt and JavaFX
 *
 * Ascii visible characters' key code were same
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/5 21:03
 * @version: 0.0.1
 */
enum class QtKeyCode(private val qtCode: Int, private val javafxCode: Int) {
    ESCAPE(0x01000000, KeyCode.ESCAPE.code),
    TAB(0x01000001, KeyCode.TAB.code),
    BACKSPACE(0x01000003, KeyCode.BACK_SPACE.code),
    ENTER(0x01000005, KeyCode.ENTER.code),
    INSERT(0x01000006, KeyCode.INSERT.code),
    DELETE(0x01000007, KeyCode.DELETE.code),
    PAUSE(0x01000008, KeyCode.PAUSE.code),
    PRINT(0x01000009, KeyCode.PRINTSCREEN.code),
    CLEAR(0x0100000b, KeyCode.CLEAR.code),
    HOME(0x01000010, KeyCode.HOME.code),
    END(0x01000011, KeyCode.END.code),
    LEFT(0x01000012, KeyCode.LEFT.code),
    UP(0x01000013, KeyCode.UP.code),
    RIGHT(0x01000014, KeyCode.RIGHT.code),
    DOWN(0x01000015, KeyCode.DOWN.code),
    PAGE_UP(0x01000016, KeyCode.PAGE_UP.code),
    PAGE_DOWN(0x01000017, KeyCode.PAGE_DOWN.code),
    SHIFT(0x01000020, KeyCode.SHIFT.code),
    CONTROL(0x01000021, KeyCode.CONTROL.code),
    META(0x01000022, KeyCode.META.code),
    ALT(0x01000023, KeyCode.ALT.code),
    CAPS_LOCK(0x01000024, KeyCode.CAPS.code),
    NUMBER_LOCK(0x01000025, KeyCode.NUM_LOCK.code),
    SCROLL_LOCK(0x01000026, KeyCode.SCROLL_LOCK.code),
    F1(0x01000030, KeyCode.F1.code),
    F2(0x01000031, KeyCode.F2.code),
    F3(0x01000032, KeyCode.F3.code),
    F4(0x01000033, KeyCode.F4.code),
    F5(0x01000034, KeyCode.F5.code),
    F6(0x01000035, KeyCode.F6.code),
    F7(0x01000036, KeyCode.F7.code),
    F8(0x01000037, KeyCode.F8.code),
    F9(0x01000038, KeyCode.F9.code),
    F10(0x01000039, KeyCode.F10.code),
    F11(0x0100003a, KeyCode.F11.code),
    F12(0x0100003b, KeyCode.F12.code),
    F13(0x0100003c, KeyCode.F13.code),
    F14(0x0100003d, KeyCode.F14.code),
    F15(0x0100003e, KeyCode.F15.code),
    F16(0x0100003f, KeyCode.F16.code),
    F17(0x01000040, KeyCode.F17.code),
    F18(0x01000041, KeyCode.F18.code),
    F19(0x01000042, KeyCode.F19.code),
    F20(0x01000043, KeyCode.F20.code),
    F21(0x01000044, KeyCode.F21.code),
    F22(0x01000045, KeyCode.F22.code),
    F23(0x01000046, KeyCode.F23.code),
    F24(0x01000047, KeyCode.F24.code),
    ;

    companion object {
        @JvmStatic
        fun getQtCode(javafxCode: Int): Int {
            for (code in values()) {
                if (javafxCode == code.javafxCode) {
                    return code.qtCode
                }
            }
            return javafxCode
        }
    }
}