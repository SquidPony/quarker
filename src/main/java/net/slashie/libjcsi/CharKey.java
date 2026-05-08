package net.slashie.libjcsi;

public class CharKey {
    public static final int ESC = 27;
    public static final int ENTER = 10;
    public static final int DOT = '.';
    public static final int MORETHAN = '>';
    public static final int LESSTHAN = '<';
    public static final int QUESTION = '?';
    public static final int S = 's';
    public static final int P = 'p';
    public static final int R = 'r';
    public static final int L = 'l';
    public static final int B = 'b';
    public static final int h = 'h';
    public static final int j = 'j';
    public static final int k = 'k';
    public static final int l = 'l';
    public static final int y = 'y';
    public static final int u = 'u';
    public static final int b = 'b';
    public static final int n = 'n';

    public static final int ARROW_UP = 1001;
    public static final int ARROW_UP_RIGHT = 1002;
    public static final int ARROW_RIGHT = 1003;
    public static final int ARROW_DOWN_RIGHT = 1004;
    public static final int ARROW_DOWN = 1005;
    public static final int ARROW_DOWN_LEFT = 1006;
    public static final int ARROW_LEFT = 1007;
    public static final int ARROW_UP_LEFT = 1008;
    public static final int ARROW_SELF = 1009;

    public int code;

    public CharKey() {
        code = 0;
    }

    public CharKey(int code) {
        this.code = code;
    }

    public boolean isArrow() {
        return code >= ARROW_UP && code <= ARROW_SELF;
    }

    public boolean isUpArrow() {
        return code == ARROW_UP;
    }

    public boolean isUpRightArrow() {
        return code == ARROW_UP_RIGHT;
    }

    public boolean isRightArrow() {
        return code == ARROW_RIGHT;
    }

    public boolean isDownRightArrow() {
        return code == ARROW_DOWN_RIGHT;
    }

    public boolean isDownArrow() {
        return code == ARROW_DOWN;
    }

    public boolean isDownLeftArrow() {
        return code == ARROW_DOWN_LEFT;
    }

    public boolean isLeftArrow() {
        return code == ARROW_LEFT;
    }

    public boolean isUpLeftArrow() {
        return code == ARROW_UP_LEFT;
    }

    public boolean isSelfArrow() {
        return code == ARROW_SELF;
    }
}