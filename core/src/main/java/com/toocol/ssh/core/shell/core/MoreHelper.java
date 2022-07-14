package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.utilities.utils.CharUtil;

/**
 * the helper of linux command 'more'
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/17 15:51
 * @version:
 */
public record MoreHelper() {

    public boolean support(char inChar) {
        for (SupportChar support : SupportChar.values()) {
            if (inChar == support.ch && !support.subCmd) {
                return true;
            }
        }
        return false;
    }

    public boolean supportSub(char inChar) {
        for (SupportChar support : SupportChar.values()) {
            if (inChar == support.ch && support.subCmd) {
                return true;
            }
        }
        return false;
    }

    public boolean supportEdit(char inChar) {
        return CharUtil.isAsciiPrintable(inChar);
    }

    public enum SupportChar {
        h('h', "more help", false),
        SPACE(CharUtil.SPACE, "Display next k lines of text [current screen size]", false),
        z('z', "Display next k lines of text [current screen size]*", false),
        RETURN(CharUtil.CR, "Display next k lines of text [1]*", false),
        d('d', "Scroll k lines [current scroll size, initially 11]*", false),
        CTRL_D(CharUtil.CTRL_D, "Scroll k lines [current scroll size, initially 11]*", false),
        q('q', "Exit from more", false),
        Q('Q', "Exit from more", false),
        INTERRUPT(CharUtil.CTRL_C, "Exit from more", false),
        s('s', "Skip forward k lines of text [1]", false),
        f('f', "Skip forward k screenfuls of text [1]", false),
        b('b', "Skip backwards k screenfuls of text [1]", false),
        CTRL_B(CharUtil.CTRL_B, "Skip backwards k screenfuls of text [1]", false),
        SINGLE_QUOTE('\'', "Go to place where previous search started", false),
        REGULAR_EXPRESSION('/', "Search for kth occurrence of regular expression [1]", false),
        n('n', "Search for kth occurrence of last r.e [1]", false),
        CMD_CXCLA('!', "Execute <cmd> in a subshell", false),
        CTRL_L(CharUtil.CTRL_L, "Redraw screen", false),
        CMD_COLON(':', "Execute <cmd> in a subshell", false),
        COLON_N('n', "Go to kth next file [1]", true),
        COLON_P('p', "Go to kth previous file [1]", true),
        COLON_F('f', "Display current file name and line number", true),
        DOT(CharUtil.DOT, "Repeat previous command", false);

        public final char ch;
        public final String comment;
        public final boolean subCmd;

        SupportChar(char ch, String comment, Boolean subCmd) {
            this.ch = ch;
            this.comment = comment;
            this.subCmd = subCmd;
        }
    }

}
