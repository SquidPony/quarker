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
        StringBuilder screen = new StringBuilder((xdim + 1) * ydim);
        for (int y = 0; y < ydim; y++) {
            for (int x = 0; x < xdim; x++) {
                screen.append(chars[x][y]);
            }
            if (y + 1 < ydim) {
                screen.append('\n');
            }
        }
        setScreenText(screen.toString());
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

    @JSBody(params = "text", script = "var el = document.getElementById('quarker-screen'); if (el) el.textContent = text;")
    private static native void setScreenText(String text);

    @JSBody(params = "text", script = "var el = document.getElementById('status'); if (el) el.textContent = text;")
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

    @JSBody(script = "if (!window.__quarkerInputQueue) {"
            + "window.__quarkerInputQueue = [];"
            + "window.addEventListener('keydown', function(e){"
            + "var key = e.key || '';"
            + "if (!key) { return; }"
            + "window.__quarkerInputQueue.push(key);"
            + "e.preventDefault();"
            + "});"
            + "}")
    private static native void initInputQueue();

    @JSBody(script = "var q = window.__quarkerInputQueue; if (!q || q.length === 0) { return ''; } return String(q.shift());")
    private static native String pollInputKey();
}
