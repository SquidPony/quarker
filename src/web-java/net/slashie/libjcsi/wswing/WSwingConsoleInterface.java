package net.slashie.libjcsi.wswing;

import net.slashie.libjcsi.CSIColor;
import net.slashie.libjcsi.CharKey;
import org.teavm.jso.JSBody;

public class WSwingConsoleInterface {
    public final int xdim;
    public final int ydim;

    private char[][] chars;
    private CSIColor[][] fronts;
    private CSIColor[][] backs;
    private char[][] savedChars;
    private CSIColor[][] savedFronts;
    private CSIColor[][] savedBacks;

    private int caretX = 0;
    private int caretY = 0;

    public WSwingConsoleInterface(String title) {
        this(title, 90, 30);
    }

    public WSwingConsoleInterface(String title, int xdim, int ydim) {
        this.xdim = xdim;
        this.ydim = ydim;
        chars = new char[xdim][ydim];
        fronts = new CSIColor[xdim][ydim];
        backs = new CSIColor[xdim][ydim];
        clearArrays(chars, fronts, backs);
        initInputQueue();
        setStatus("Running.");
    }

    public void cls() {
        clearArrays(chars, fronts, backs);
    }

    public void refresh() {
        StringBuilder screen = new StringBuilder((xdim + 1) * ydim * 3);
        for (int y = 0; y < ydim; y++) {
            int x = 0;
            while (x < xdim) {
                CSIColor fg = fronts[x][y] == null ? CSIColor.WHITE : fronts[x][y];
                CSIColor bg = backs[x][y] == null ? CSIColor.BLACK : backs[x][y];
                int runEnd = x + 1;
                while (runEnd < xdim
                        && colorsEqual(fronts[runEnd][y], fg)
                        && colorsEqual(backs[runEnd][y], bg)) {
                    runEnd++;
                }

                screen.append("<span style=\"color:")
                        .append(toCssColor(fg))
                        .append(";background:")
                        .append(toCssColor(bg))
                    .append("\">");
                for (int i = x; i < runEnd; i++) {
                    appendEscaped(screen, chars[i][y]);
                }
                screen.append("</span>");
                x = runEnd;
            }
            if (y + 1 < ydim) {
                screen.append('\n');
            }
        }
        setScreenHtml(screen.toString());
    }

    public void saveBuffer() {
        savedChars = deepCopyChars(chars);
        savedFronts = deepCopyColors(fronts);
        savedBacks = deepCopyColors(backs);
    }

    public void restore() {
        if (savedChars == null) {
            return;
        }
        chars = deepCopyChars(savedChars);
        fronts = deepCopyColors(savedFronts);
        backs = deepCopyColors(savedBacks);
    }

    public void locateCaret(int x, int y) {
        caretX = Math.max(0, Math.min(xdim - 1, x));
        caretY = Math.max(0, Math.min(ydim - 1, y));
    }

    public CharKey inkey() {
        String key = readKeyToken();
        if (key.isEmpty()) {
            return new CharKey(CharKey.ENTER);
        }

        String normalized = key.toLowerCase();
        if ("esc".equals(normalized) || "escape".equals(normalized)) {
            return new CharKey(CharKey.ESC);
        }
        if ("up".equals(normalized) || "arrowup".equals(normalized)) {
            return new CharKey(CharKey.ARROW_UP);
        }
        if ("right".equals(normalized) || "arrowright".equals(normalized)) {
            return new CharKey(CharKey.ARROW_RIGHT);
        }
        if ("down".equals(normalized) || "arrowdown".equals(normalized)) {
            return new CharKey(CharKey.ARROW_DOWN);
        }
        if ("left".equals(normalized) || "arrowleft".equals(normalized)) {
            return new CharKey(CharKey.ARROW_LEFT);
        }
        if ("self".equals(normalized) || "wait".equals(normalized)) {
            return new CharKey(CharKey.ARROW_SELF);
        }

        char ch = key.charAt(0);
        if (Character.isUpperCase(ch)) {
            return new CharKey(ch);
        }
        return new CharKey(Character.toLowerCase(ch));
    }

    public String input() {
        StringBuilder value = new StringBuilder();
        setStatus("Type text and press Enter.");
        while (true) {
            String key = readKeyToken();
            if ("enter".equalsIgnoreCase(key)) {
                setStatus("Running.");
                return value.toString();
            }
            if ("backspace".equalsIgnoreCase(key)) {
                if (value.length() > 0) {
                    value.deleteCharAt(value.length() - 1);
                }
            } else if (key.length() == 1) {
                value.append(key.charAt(0));
            }
            setStatus("Input: " + value);
        }
    }

    public void print(int x, int y, char ch, CSIColor front) {
        print(x, y, ch, front, CSIColor.BLACK);
    }

    public void print(int x, int y, char ch, CSIColor front, CSIColor back) {
        if (x < 0 || x >= xdim || y < 0 || y >= ydim) {
            return;
        }
        chars[x][y] = ch;
        fronts[x][y] = front == null ? CSIColor.WHITE : front;
        backs[x][y] = back == null ? CSIColor.BLACK : back;
    }

    public String saveScreenshot(String directoryName) {
        return "";
    }

    public String startRecording(String directoryName) {
        return "";
    }

    public int captureRecordingFrame() {
        return 0;
    }

    public String stopRecording(String directoryName) {
        return "";
    }

    public void finalizeRecordingOnShutdown(String directoryName) {
    }

    public boolean hasPendingRecordingFrames(String directoryName) {
        return false;
    }

    public String recoverPendingRecordings(String directoryName) {
        return "";
    }

    public void discardPendingRecordings(String directoryName) {
    }

    private static void clearArrays(char[][] c, CSIColor[][] f, CSIColor[][] b) {
        for (int x = 0; x < c.length; x++) {
            for (int y = 0; y < c[x].length; y++) {
                c[x][y] = ' ';
                f[x][y] = CSIColor.WHITE;
                b[x][y] = CSIColor.BLACK;
            }
        }
    }

    private static char[][] deepCopyChars(char[][] src) {
        char[][] out = new char[src.length][src[0].length];
        for (int x = 0; x < src.length; x++) {
            System.arraycopy(src[x], 0, out[x], 0, src[x].length);
        }
        return out;
    }

    private static CSIColor[][] deepCopyColors(CSIColor[][] src) {
        CSIColor[][] out = new CSIColor[src.length][src[0].length];
        for (int x = 0; x < src.length; x++) {
            System.arraycopy(src[x], 0, out[x], 0, src[x].length);
        }
        return out;
    }

    private static boolean colorsEqual(CSIColor a, CSIColor b) {
        CSIColor left = a == null ? CSIColor.WHITE : a;
        CSIColor right = b == null ? CSIColor.WHITE : b;
        return left.equals(right);
    }

    private static void appendEscaped(StringBuilder out, char c) {
        if (c == '&') {
            out.append("&amp;");
        } else if (c == '<') {
            out.append("&lt;");
        } else if (c == '>') {
            out.append("&gt;");
        } else {
            out.append(c);
        }
    }

    private static String toCssColor(CSIColor c) {
        CSIColor color = c == null ? CSIColor.WHITE : c;
        return "#" + hex(color.getR()) + hex(color.getG()) + hex(color.getB());
    }

    private static String hex(int value) {
        final char[] chars = "0123456789abcdef".toCharArray();
        int v = Math.max(0, Math.min(255, value));
        return String.valueOf(new char[]{chars[(v >> 4) & 0xF], chars[v & 0xF]});
    }

    @JSBody(params = "html", script = "var root = document.querySelector('[data-quarker-root]') || document;"
            + "var el = root.querySelector('[data-quarker-screen]') || document.getElementById('quarker-screen');"
            + "if (el) { el.innerHTML = html; }")
    private static native void setScreenHtml(String html);

    @JSBody(params = "text", script = "var root = document.querySelector('[data-quarker-root]') || document;"
            + "var el = root.querySelector('[data-quarker-status]') || document.getElementById('status');"
            + "if (el) { el.textContent = text; }")
    private static native void setStatus(String text);

    private static String readKeyToken() {
        String key;
        do {
            key = pollInputKey();
            if (!key.isEmpty()) {
                return key;
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException ignored) {
            }
        } while (true);
    }

    @JSBody(script = "var root = document.querySelector('[data-quarker-root]') || document;"
            + "if (!root.__quarkerInputQueue) { root.__quarkerInputQueue = []; }"
            + "if (!root.__quarkerInputBound) {"
            + "root.__quarkerInputBound = true;"
            + "var target = root.querySelector('.quarker-frame') || window;"
            + "target.addEventListener('keydown', function(e){"
            + "var key = e.key || '';"
            + "if (!key) { return; }"
            + "root.__quarkerInputQueue.push(key);"
            + "e.preventDefault();"
            + "});"
            + "if (target !== window) {"
            + "target.addEventListener('click', function(){ if (target.focus) { target.focus(); } });"
            + "if (target.focus) { target.focus(); }"
            + "}"
            + "}")
    private static native void initInputQueue();

    @JSBody(script = "var root = document.querySelector('[data-quarker-root]') || document;"
            + "var q = root.__quarkerInputQueue;"
            + "if (!q || q.length === 0) { return ''; }"
            + "return String(q.shift());")
    private static native String pollInputKey();
}
