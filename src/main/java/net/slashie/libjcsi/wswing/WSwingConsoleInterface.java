package net.slashie.libjcsi.wswing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.slashie.libjcsi.CSIColor;
import net.slashie.libjcsi.CharKey;

public class WSwingConsoleInterface {
    public final int xdim;
    public final int ydim;

    private final JFrame frame;
    private final ConsolePanel panel;
    private final JTextField inputField;
    private final BlockingQueue<CharKey> keyQueue = new LinkedBlockingQueue<CharKey>();
    private volatile String lineInput = "";

    private int inputStartX = 0;
    private int inputStartY = 0;
    private int lastEchoedLength = 0;

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
        this.chars = new char[xdim][ydim];
        this.fronts = new CSIColor[xdim][ydim];
        this.backs = new CSIColor[xdim][ydim];
        clearArrays(chars, fronts, backs);

        final JFrame[] tmpFrame = new JFrame[1];
        final ConsolePanel[] tmpPanel = new ConsolePanel[1];
        final JTextField[] tmpInput = new JTextField[1];

        runOnEdtAndWait(new Runnable() {
            @Override
            public void run() {
                JFrame f = new JFrame(title);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                ConsolePanel p = new ConsolePanel();
                p.setPreferredSize(new Dimension(p.getCellWidth() * WSwingConsoleInterface.this.xdim,
                        p.getCellHeight() * WSwingConsoleInterface.this.ydim));
                p.setFocusable(true);

                JTextField in = new JTextField();
                in.addActionListener(e -> {
                    lineInput = in.getText();
                    in.setText("");
                    keyQueue.offer(new CharKey(CharKey.ENTER));
                });
                in.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            return;
                        }
                        if (isGameKey(e.getKeyCode())) {
                            e.consume();
                            keyQueue.offer(toCharKey(e));
                        }
                    }

                    private boolean isGameKey(int keyCode) {
                        switch (keyCode) {
                            case KeyEvent.VK_ESCAPE:
                            case KeyEvent.VK_UP:
                            case KeyEvent.VK_DOWN:
                            case KeyEvent.VK_LEFT:
                            case KeyEvent.VK_RIGHT:
                            case KeyEvent.VK_NUMPAD1:
                            case KeyEvent.VK_NUMPAD2:
                            case KeyEvent.VK_NUMPAD3:
                            case KeyEvent.VK_NUMPAD4:
                            case KeyEvent.VK_NUMPAD5:
                            case KeyEvent.VK_NUMPAD6:
                            case KeyEvent.VK_NUMPAD7:
                            case KeyEvent.VK_NUMPAD8:
                            case KeyEvent.VK_NUMPAD9:
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                p.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        keyQueue.offer(toCharKey(e));
                    }
                });

                f.setLayout(new BorderLayout());
                f.add(new JScrollPane(p), BorderLayout.CENTER);
                f.add(in, BorderLayout.SOUTH);
                f.pack();
                f.setLocationRelativeTo(null);
                f.setVisible(true);
                p.requestFocusInWindow();

                tmpFrame[0] = f;
                tmpPanel[0] = p;
                tmpInput[0] = in;
            }
        });

        this.frame = tmpFrame[0];
        this.panel = tmpPanel[0];
        this.inputField = tmpInput[0];
    }

    public void cls() {
        clearArrays(chars, fronts, backs);
    }

    public void refresh() {
        runOnEdt(new Runnable() {
            @Override
            public void run() {
                panel.repaint();
            }
        });
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
        runOnEdt(() -> panel.requestFocusInWindow());
    }

    public void locateCaret(int x, int y) {
        caretX = Math.max(0, Math.min(xdim - 1, x));
        caretY = Math.max(0, Math.min(ydim - 1, y));
        inputStartX = caretX;
        inputStartY = caretY;
        runOnEdt(() -> inputField.requestFocusInWindow());
    }

    public CharKey inkey() {
        try {
            return keyQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CharKey(CharKey.ESC);
        }
    }

    public String input() {
        runOnEdtAndWait(() -> {
            inputField.setText("");
            inputField.requestFocusInWindow();
            inputStartX = caretX;
            inputStartY = caretY;
            lastEchoedLength = 0;
        });

        DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                echoInputToConsole();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                echoInputToConsole();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                echoInputToConsole();
            }

            private void echoInputToConsole() {
                runOnEdt(() -> {
                    String currentText = inputField.getText();
                    int currentLength = currentText.length();

                    // Clear old text by printing spaces where the old text was
                    for (int i = 0; i < lastEchoedLength; i++) {
                        print(inputStartX + i, inputStartY, ' ', CSIColor.WHITE, CSIColor.BLACK);
                    }

                    // Print new text
                    for (int i = 0; i < currentLength; i++) {
                        print(inputStartX + i, inputStartY, currentText.charAt(i), CSIColor.WHITE, CSIColor.BLACK);
                    }

                    lastEchoedLength = currentLength;
                    refresh();
                });
            }
        };

        inputField.getDocument().addDocumentListener(docListener);

        try {
            while (true) {
                CharKey key = inkey();
                if (key.code == CharKey.ENTER) {
                    String result = lineInput == null ? "" : lineInput;
                    lineInput = "";
                    return result;
                }
            }
        } finally {
            inputField.getDocument().removeDocumentListener(docListener);
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

    private static CharKey toCharKey(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                return new CharKey(CharKey.ESC);
            case KeyEvent.VK_ENTER:
                return new CharKey(CharKey.ENTER);
            case KeyEvent.VK_UP:
                return new CharKey(CharKey.ARROW_UP);
            case KeyEvent.VK_RIGHT:
                return new CharKey(CharKey.ARROW_RIGHT);
            case KeyEvent.VK_DOWN:
                return new CharKey(CharKey.ARROW_DOWN);
            case KeyEvent.VK_LEFT:
                return new CharKey(CharKey.ARROW_LEFT);
            case KeyEvent.VK_NUMPAD5:
                return new CharKey(CharKey.ARROW_SELF);
            case KeyEvent.VK_NUMPAD7:
                return new CharKey(CharKey.ARROW_UP_LEFT);
            case KeyEvent.VK_NUMPAD8:
                return new CharKey(CharKey.ARROW_UP);
            case KeyEvent.VK_NUMPAD9:
                return new CharKey(CharKey.ARROW_UP_RIGHT);
            case KeyEvent.VK_NUMPAD6:
                return new CharKey(CharKey.ARROW_RIGHT);
            case KeyEvent.VK_NUMPAD3:
                return new CharKey(CharKey.ARROW_DOWN_RIGHT);
            case KeyEvent.VK_NUMPAD2:
                return new CharKey(CharKey.ARROW_DOWN);
            case KeyEvent.VK_NUMPAD1:
                return new CharKey(CharKey.ARROW_DOWN_LEFT);
            case KeyEvent.VK_NUMPAD4:
                return new CharKey(CharKey.ARROW_LEFT);
            default:
                char ch = Character.toLowerCase(e.getKeyChar());
                if (ch == KeyEvent.CHAR_UNDEFINED) {
                    return new CharKey();
                }
                return new CharKey(ch);
        }
    }

    private static void runOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    private static void runOnEdtAndWait(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize Swing UI", e);
        }
    }

    private final class ConsolePanel extends JPanel {
        private final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        private final int cellWidth;
        private final int cellHeight;

        private ConsolePanel() {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.getGraphics();
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            cellWidth = fm.charWidth('W');
            cellHeight = fm.getHeight();
            g.dispose();
        }

        private int getCellWidth() {
            return cellWidth;
        }

        private int getCellHeight() {
            return cellHeight;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setFont(font);

            FontMetrics fm = g.getFontMetrics();
            int baselineAdjust = fm.getAscent();
            for (int x = 0; x < xdim; x++) {
                for (int y = 0; y < ydim; y++) {
                    CSIColor back = backs[x][y];
                    CSIColor front = fronts[x][y];

                    g.setColor(new Color(back.getR(), back.getG(), back.getB()));
                    g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

                    g.setColor(new Color(front.getR(), front.getG(), front.getB()));
                    g.drawString(String.valueOf(chars[x][y]), x * cellWidth, (y * cellHeight) + baselineAdjust);
                }
            }
        }
    }
}