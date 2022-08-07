package com.toocol.termio.utilities.anis;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/7 22:40
 * @version: 0.0.1
 */
public enum KeyBoardString {
    F1("F1", "0;59", "0;84", "0;94", "0;104"),
    F2("F2", "0;60", "0;85", "0;95", "0;105"),
    F3("F3", "0;61", "0;86", "0;96", "0;106"),
    F4("F4", "0;62", "0;87", "0;97", "0;107"),
    F5("F5", "0;63", "0;88", "0;98", "0;108"),
    F6("F6", "0;64", "0;89", "0;99", "0;109"),
    F7("F7", "0;65", "0;90", "0;100", "0;110"),
    F8("F8", "0;66", "0;91", "0;101", "0;111"),
    F9("F9", "0;67", "0;92", "0;102", "0;112"),
    F10("F10", "0;68", "0;93", "0;103", "0;113"),
    F11("F11", "0;133", "0;135", "0;137", "0;139"),
    F12("F12", "0;134", "0;136", "0;138", "0;140"),
    HOME_NUM_KEYPAD("HOME (num keypad)", "0;71", "55", "0;119", null),
    UP_ARROW_NUM_KEYPAD("UP ARROW (num keypad)", "0;72", "56", "0;141", null),
    PAGE_UP_NUM_KEYPAD("PAGE UP (num keypad)", "0;73", "57", "0;132", null),
    LEFT_ARROW_NUM_KEYPAD("LEFT ARROW (num keypad)", "0;75", "52", "0;115", null),
    RIGHT_ARROW_NUM_KEYPAD("RIGHT ARROW (num keypad)", "0;77", "54", "0;116", null),
    END_NUM_KEYPAD("END (num keypad)", "0;79", "49", "0;117", null),
    DOWN_ARROW_NUM_KEYPAD("DOWN ARROW (num keypad)", "0;80", "50", "0;145", null),
    PAGE_DOWN_NUM_KEYPAD("PAGE DOWN (num keypad)", "0;81", "51", "0;118", null),
    INSERT_NUM_KEYPAD("INSERT (num keypad)", "0;82", "48", "0;146", null),
    DELETE_NUM_KEYPAD("DELETE (num keypad)", "0;83", "46", "0;147", null),
    HOME("HOME", "224;71", "224;71", "224;119", "224;151"),
    UP_ARROW("UP ARROW", "224;72", "224;72", "224;141", "224;152"),
    PAGE_UP("PAGE UP", "224;73", "224;73", "224;132", "224;153"),
    LEFT_ARROW("LEFT ARROW", "224;75", "224;75", "224;115", "224;155"),
    RIGHT_ARROW("RIGHT ARROW", "224;77", "224;77", "224;116", "224;157"),
    END("END", "224;79", "224;79", "224;117", "224;159"),
    DOWN_ARROW("DOWN ARROW", "224;80", "224;80", "224;145", "224;154"),
    PAGE_DOWN("PAGE DOWN", "224;81", "224;81", "224;118", "224;161"),
    INSERT("INSERT", "224;82", "224;82", "224;146", "224;162"),
    DELETE("DELETE", "224;83", "224;83", "224;147", "224;163"),
    PRINT_SCREEN("PRINT SCREEN", null, null, "0;114", null),
    PAUSE_BREAK("PAUSE/BREAK", null, null, "0;0", null),
    BACKSPACE("BACKSPACE", "8", "8", "127", "0"),
    ENTER("ENTER", "13", null, "10", "1"),
    TAB("TAB", "9", "0;15", "0;148", "0;165"),
    NULL("NULL", "0;3", null, null, null),
    A("A", "97", "65", "1", "0;30"),
    B("B", "98", "66", "2", "0;48"),
    C("C", "99", "66", "3", "0;46"),
    D("D", "100", "68", "4", "0;32"),
    E("E", "101", "69", "5", "0;18"),
    F("F", "102", "70", "6", "0;33"),
    G("G", "103", "71", "7", "0;34"),
    H("H", "104", "72", "8", "0;35"),
    I("I", "105", "73", "9", "0;23"),
    J("J", "106", "74", "10", "0;36"),
    K("K", "107", "75", "11", "0;37"),
    L("L", "108", "76", "12", "0;38"),
    M("M", "109", "77", "13", "0;50"),
    N("N", "110", "78", "14", "0;49"),
    O("O", "111", "79", "15", "0;24"),
    P("P", "112", "80", "16", "0;25"),
    Q("Q", "113", "81", "17", "0;16"),
    R("R", "114", "82", "18", "0;19"),
    S("S", "115", "83", "19", "0;31"),
    T("T", "116", "84", "20", "0;20"),
    U("U", "117", "85", "21", "0;22"),
    V("V", "118", "86", "22", "0;47"),
    W("W", "119", "87", "23", "0;17"),
    X("X", "120", "88", "24", "0;45"),
    Y("Y", "121", "89", "25", "0;21"),
    Z("Z", "122", "90", "26", "0;44"),
    KEY_1("1", "49", "33", null, "0;120"),
    KEY_2("2", "50", "64", "0", "0;121"),
    KEY_3("3", "51", "35", null, "0;122"),
    KEY_4("4", "52", "36", null, "0;123"),
    KEY_5("5", "53", "37", null, "0;124"),
    KEY_6("6", "54", "94", "30", "0;125"),
    KEY_7("7", "55", "38", null, "0;126"),
    KEY_8("8", "56", "42", null, "0;126"),
    KEY_9("9", "57", "40", null, "0;127"),
    KEY_0("0", "48", "41", null, "0;129"),
    KEY_BAR("-", "45", "95", "31", "0;130"),
    KEY_EQUAL("=", "61", "43", "---", "0;131"),
    KEY_LEFT_BRACKET("[", "91", "123", "27", "0;26"),
    KEY_RIGHT_BRACKET("]", "93", "125", "29", "0;27"),
    KEY_BLANK(" ", "92", "124", "28", "0;43"),
    KEY_SEMI(";", "59", "58", null, "0;39"),
    KEY_SINGLE_QUOTE("'", "39", "34", null, "0;40"),
    KEY_COMMA(",", "44", "60", null, "0;51"),
    KEY_DASH(".", "46", "62", null, "0;52"),
    KEY_SLASH("/", "47", "63", null, "0;53"),
    KEY_POINT("`", "96", "126", null, "0;41"),
    ENTER_KEYPAD("ENTER (keypad)", "13", null, "10", "0;166"),
    KEY_SLASH_KEYPAD("/ (keypad)", "47", "47", "0;142", "0;74"),
    KEY_MUL_KEYPAD("* (keypad)", "42", "0;144", "0;78", null),
    KEY_MINUS_KEYPAD("- (keypad)", "45", "45", "0;149", "0;164"),
    KEY_PLUS_KEYPAD("+ (keypad)", "43", "43", "0;150", "0;55"),
    KEY_5_KEYPAD("5 (keypad)", "0;76", "53", "0;143", null),
    ;
    public final String key;
    public final String code;
    public final String shiftCode;
    public final String ctrlCode;
    public final String altCode;

    KeyBoardString(String key, String code, String shiftCode, String ctrlCode, String altCode) {
        this.key = key;
        this.code = code;
        this.shiftCode = shiftCode;
        this.ctrlCode = ctrlCode;
        this.altCode = altCode;
    }
}
