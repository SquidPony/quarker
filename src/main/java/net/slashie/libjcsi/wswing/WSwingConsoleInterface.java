package net.slashie.libjcsi.wswing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
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

    private static final String TEMP_RECORDING_DIR = ".temp";

    private boolean recordingActive = false;
    private File recordingFramesDir = null;
    private String recordingSessionStamp = null;
    private int recordingFrameIndex = 0;

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
                f.setResizable(true);

                ConsolePanel p = new ConsolePanel();
                p.setPreferredSize(new Dimension(p.getDefaultCellWidth() * WSwingConsoleInterface.this.xdim,
                        p.getDefaultCellHeight() * WSwingConsoleInterface.this.ydim));
                p.setFocusable(true);

                // Repaint on resize so font scales to fit
                p.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        p.repaint();
                    }
                });

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

    public String saveScreenshot(String directoryName) {
        final String[] outputPath = new String[1];
        final RuntimeException[] failure = new RuntimeException[1];

        runOnEdtAndWait(() -> {
            try {
                File directory = new File(directoryName);
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IOException("Unable to create screenshot directory: " + directory.getPath());
                }

                String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
                File outputFile = new File(directory, "quarker-" + timestamp + ".png");

                int width = Math.max(1, panel.getWidth());
                int height = Math.max(1, panel.getHeight());
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                panel.paint(graphics);
                graphics.dispose();

                if (!ImageIO.write(image, "png", outputFile)) {
                    throw new IOException("No PNG image writer available");
                }

                outputPath[0] = outputFile.getAbsolutePath();
            } catch (IOException e) {
                failure[0] = new RuntimeException("Unable to save screenshot", e);
            }
        });

        if (failure[0] != null) {
            throw failure[0];
        }
        return outputPath[0];
    }

    public String startRecording(String directoryName) {
        final String[] outputPath = new String[1];
        final RuntimeException[] failure = new RuntimeException[1];

        runOnEdtAndWait(() -> {
            try {
                if (recordingActive) {
                    outputPath[0] = recordingFramesDir == null ? "" : recordingFramesDir.getAbsolutePath();
                    return;
                }

                File screenshotDir = new File(directoryName);
                if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
                    throw new IOException("Unable to create screenshot directory: " + screenshotDir.getPath());
                }

                File tempRoot = getTempRootDirectory(directoryName);
                if (!tempRoot.exists() && !tempRoot.mkdirs()) {
                    throw new IOException("Unable to create recording temp directory: " + tempRoot.getPath());
                }

                recordingSessionStamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
                recordingFramesDir = new File(tempRoot, "recording-" + recordingSessionStamp + "-frames");
                if (!recordingFramesDir.exists() && !recordingFramesDir.mkdirs()) {
                    throw new IOException("Unable to create recording frames directory: " + recordingFramesDir.getPath());
                }

                recordingFrameIndex = 0;
                recordingActive = true;
                outputPath[0] = recordingFramesDir.getAbsolutePath();
            } catch (IOException e) {
                failure[0] = new RuntimeException("Unable to start recording", e);
            }
        });

        if (failure[0] != null) {
            throw failure[0];
        }
        return outputPath[0];
    }

    public int captureRecordingFrame() {
        final int[] frameNumber = new int[] {0};
        final RuntimeException[] failure = new RuntimeException[1];

        runOnEdtAndWait(() -> {
            try {
                if (!recordingActive || recordingFramesDir == null) {
                    return;
                }

                recordingFrameIndex++;
                File frameFile = new File(recordingFramesDir, String.format("frame-%06d.png", recordingFrameIndex));

                int width = Math.max(1, panel.getWidth());
                int height = Math.max(1, panel.getHeight());
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                panel.paint(graphics);
                graphics.dispose();

                if (!ImageIO.write(image, "png", frameFile)) {
                    throw new IOException("No PNG image writer available");
                }

                frameNumber[0] = recordingFrameIndex;
            } catch (IOException e) {
                failure[0] = new RuntimeException("Unable to capture recording frame", e);
            }
        });

        if (failure[0] != null) {
            throw failure[0];
        }
        return frameNumber[0];
    }

    public String stopRecording(String directoryName) {
        final String[] outputPath = new String[1];
        final RuntimeException[] failure = new RuntimeException[1];

        runOnEdtAndWait(() -> {
            try {
                if (!recordingActive) {
                    outputPath[0] = "";
                    return;
                }

                recordingActive = false;

                File screenshotDir = new File(directoryName);
                if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
                    throw new IOException("Unable to create screenshot directory: " + screenshotDir.getPath());
                }

                String stamp = recordingSessionStamp == null
                        ? new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date())
                        : recordingSessionStamp;
                File gifFile = new File(screenshotDir, "quarker-recording-" + stamp + ".gif");

                List<File> frameFiles = listFrameFiles(recordingFramesDir);
                if (frameFiles.isEmpty()) {
                    outputPath[0] = "";
                } else {
                    writeAnimatedGif(frameFiles, gifFile, 220, true);
                    outputPath[0] = gifFile.getAbsolutePath();
                }

                deleteDirectory(recordingFramesDir);
                cleanupTempRootIfEmpty(directoryName);
                recordingFramesDir = null;
                recordingSessionStamp = null;
                recordingFrameIndex = 0;
            } catch (IOException e) {
                failure[0] = new RuntimeException("Unable to stop recording", e);
            }
        });

        if (failure[0] != null) {
            throw failure[0];
        }
        return outputPath[0];
    }

    public boolean hasPendingRecordingFrames(String directoryName) {
        File tempRoot = getTempRootDirectory(directoryName);
        List<File> pendingDirs = listPendingRecordingDirectories(tempRoot);
        return !pendingDirs.isEmpty();
    }

    public String recoverPendingRecordings(String directoryName) {
        File screenshotDir = new File(directoryName);
        if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
            throw new RuntimeException("Unable to create screenshot directory: " + screenshotDir.getPath());
        }

        File tempRoot = getTempRootDirectory(directoryName);
        List<File> pendingDirs = listPendingRecordingDirectories(tempRoot);
        List<String> outputs = new ArrayList<String>();

        for (File frameDir : pendingDirs) {
            try {
                List<File> frameFiles = listFrameFiles(frameDir);
                if (frameFiles.isEmpty()) {
                    continue;
                }

                String stamp = extractRecordingStamp(frameDir.getName());
                File gifFile = new File(screenshotDir, "quarker-recording-" + stamp + ".gif");
                writeAnimatedGif(frameFiles, gifFile, 220, true);
                outputs.add(gifFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Unable to recover pending recording from " + frameDir.getPath(), e);
            } finally {
                deleteDirectory(frameDir);
            }
        }

        cleanupTempRootIfEmpty(directoryName);
        if (outputs.isEmpty()) {
            return "";
        }
        return String.join("; ", outputs);
    }

    public void discardPendingRecordings(String directoryName) {
        File tempRoot = getTempRootDirectory(directoryName);
        deleteDirectory(tempRoot);
        recordingFramesDir = null;
        recordingSessionStamp = null;
        recordingFrameIndex = 0;
        recordingActive = false;
    }

    private static File getTempRootDirectory(String directoryName) {
        return new File(new File(directoryName), TEMP_RECORDING_DIR);
    }

    private static String extractRecordingStamp(String frameDirectoryName) {
        final String prefix = "recording-";
        final String suffix = "-frames";
        if (frameDirectoryName.startsWith(prefix) && frameDirectoryName.endsWith(suffix)) {
            return frameDirectoryName.substring(prefix.length(), frameDirectoryName.length() - suffix.length());
        }
        return new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
    }

    private static List<File> listPendingRecordingDirectories(File tempRoot) {
        List<File> pending = new ArrayList<File>();
        if (tempRoot == null || !tempRoot.exists() || !tempRoot.isDirectory()) {
            return pending;
        }

        File[] dirs = tempRoot.listFiles(File::isDirectory);
        if (dirs == null) {
            return pending;
        }

        java.util.Arrays.sort(dirs, (a, b) -> a.getName().compareTo(b.getName()));
        for (File dir : dirs) {
            if (!listFrameFiles(dir).isEmpty()) {
                pending.add(dir);
            }
        }
        return pending;
    }

    private static void cleanupTempRootIfEmpty(String directoryName) {
        File tempRoot = getTempRootDirectory(directoryName);
        if (!tempRoot.exists() || !tempRoot.isDirectory()) {
            return;
        }
        File[] entries = tempRoot.listFiles();
        if (entries == null || entries.length == 0) {
            tempRoot.delete();
        }
    }

    private static List<File> listFrameFiles(File frameDir) {
        List<File> files = new ArrayList<File>();
        if (frameDir == null || !frameDir.exists() || !frameDir.isDirectory()) {
            return files;
        }
        File[] entries = frameDir.listFiles((d, name) -> name.endsWith(".png"));
        if (entries == null) {
            return files;
        }
        java.util.Arrays.sort(entries, (a, b) -> a.getName().compareTo(b.getName()));
        for (File file : entries) {
            files.add(file);
        }
        return files;
    }

    private static void writeAnimatedGif(List<File> frameFiles, File outputFile, int delayMs, boolean loopForever)
            throws IOException {
        if (frameFiles.isEmpty()) {
            return;
        }

        BufferedImage first = ImageIO.read(frameFiles.get(0));
        if (first == null) {
            throw new IOException("Unable to read first frame image");
        }

        ImageOutputStream outputStream = new FileImageOutputStream(outputFile);
        try (GifSequenceWriter gifWriter = new GifSequenceWriter(outputStream, first.getType(), delayMs, loopForever)) {
            gifWriter.writeToSequence(first);
            for (int i = 1; i < frameFiles.size(); i++) {
                BufferedImage frame = ImageIO.read(frameFiles.get(i));
                if (frame != null) {
                    gifWriter.writeToSequence(frame);
                }
            }
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    deleteDirectory(child);
                } else {
                    child.delete();
                }
            }
        }
        dir.delete();
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
            case KeyEvent.VK_P:
                return new CharKey(e.isShiftDown() ? CharKey.CAPITAL_P : CharKey.P);
            case KeyEvent.VK_S:
                return new CharKey(CharKey.S);
            case KeyEvent.VK_R:
                return new CharKey(CharKey.R);
            case KeyEvent.VK_L:
                return new CharKey(CharKey.L);
            case KeyEvent.VK_B:
                return new CharKey(CharKey.B);
            case KeyEvent.VK_V:
                return new CharKey(e.isShiftDown() ? CharKey.CAPITAL_V : CharKey.V);
            case KeyEvent.VK_H:
                return new CharKey(CharKey.h);
            case KeyEvent.VK_J:
                return new CharKey(CharKey.j);
            case KeyEvent.VK_K:
                return new CharKey(CharKey.k);
            case KeyEvent.VK_Y:
                return new CharKey(CharKey.y);
            case KeyEvent.VK_U:
                return new CharKey(CharKey.u);
            case KeyEvent.VK_N:
                return new CharKey(CharKey.n);
            case KeyEvent.VK_PERIOD:
                return new CharKey(e.isShiftDown() ? CharKey.MORETHAN : CharKey.DOT);
            case KeyEvent.VK_COMMA:
                return new CharKey(e.isShiftDown() ? CharKey.LESSTHAN : ',');
            case KeyEvent.VK_SLASH:
                return new CharKey(e.isShiftDown() ? CharKey.QUESTION : '/');
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
        private static final int BASE_FONT_SIZE = 16;
        private final Font baseFont = new Font(Font.MONOSPACED, Font.PLAIN, BASE_FONT_SIZE);
        private final int defaultCellWidth;
        private final int defaultCellHeight;

        private ConsolePanel() {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.getGraphics();
            g.setFont(baseFont);
            FontMetrics fm = g.getFontMetrics();
            defaultCellWidth = fm.charWidth('W');
            defaultCellHeight = fm.getHeight();
            g.dispose();
        }

        private int getDefaultCellWidth() {
            return defaultCellWidth;
        }

        private int getDefaultCellHeight() {
            return defaultCellHeight;
        }

        /** Find the largest font size where characters fit within targetW x targetH per cell. */
        private Font scaledFont(int targetW, int targetH) {
            // Use binary search over font size
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.getGraphics();
            int lo = 1, hi = Math.max(targetH, 8) * 2, best = 1;
            while (lo <= hi) {
                int mid = (lo + hi) / 2;
                Font f = new Font(Font.MONOSPACED, Font.PLAIN, mid);
                g.setFont(f);
                FontMetrics fm = g.getFontMetrics();
                if (fm.charWidth('W') <= targetW && fm.getHeight() <= targetH) {
                    best = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
            g.dispose();
            return new Font(Font.MONOSPACED, Font.PLAIN, best);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int panelW = getWidth();
            int panelH = getHeight();

            // Compute cell size that fills the whole panel
            int targetCellW = panelW / xdim;
            int targetCellH = panelH / ydim;

            Font font = scaledFont(targetCellW, targetCellH);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int cellW = fm.charWidth('W');
            int cellH = fm.getHeight();
            int baselineAdjust = fm.getAscent();

            for (int x = 0; x < xdim; x++) {
                for (int y = 0; y < ydim; y++) {
                    CSIColor back = backs[x][y];
                    CSIColor front = fronts[x][y];

                    g.setColor(new Color(back.getR(), back.getG(), back.getB()));
                    g.fillRect(x * cellW, y * cellH, cellW, cellH);

                    g.setColor(new Color(front.getR(), front.getG(), front.getB()));
                    g.drawString(String.valueOf(chars[x][y]), x * cellW, (y * cellH) + baselineAdjust);
                }
            }
        }
    }

    private static final class GifSequenceWriter implements AutoCloseable {
        private final ImageWriter gifWriter;
        private final ImageWriteParam imageWriteParam;
        private final IIOMetadata imageMetaData;

        private GifSequenceWriter(ImageOutputStream outputStream, int imageType, int timeBetweenFramesMS,
                boolean loopContinuously) throws IOException {
            gifWriter = getWriter();
            imageWriteParam = gifWriter.getDefaultWriteParam();
            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);

            imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

            String metaFormatName = imageMetaData.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

            IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(Math.max(1, timeBetweenFramesMS / 10)));
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

            IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
            commentsNode.setAttribute("CommentExtension", "Created by Quarker");

            IIOMetadataNode appEntensionsNode = getNode(root, "ApplicationExtensions");

            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");

            int loop = loopContinuously ? 0 : 1;
            child.setUserObject(new byte[] {
                    0x1,
                    (byte) (loop & 0xFF),
                    (byte) ((loop >> 8) & 0xFF)
            });
            appEntensionsNode.appendChild(child);

            imageMetaData.setFromTree(metaFormatName, root);

            gifWriter.setOutput(outputStream);
            gifWriter.prepareWriteSequence(null);
        }

        private void writeToSequence(BufferedImage img) throws IOException {
            gifWriter.writeToSequence(new javax.imageio.IIOImage(img, null, imageMetaData), imageWriteParam);
        }

        @Override
        public void close() throws IOException {
            gifWriter.endWriteSequence();
        }

        private static ImageWriter getWriter() throws IOException {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
            if (!iter.hasNext()) {
                throw new IOException("No GIF Image Writers Exist");
            }
            return iter.next();
        }

        private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
            int nNodes = rootNode.getLength();
            for (int i = 0; i < nNodes; i++) {
                if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                    return (IIOMetadataNode) rootNode.item(i);
                }
            }
            IIOMetadataNode node = new IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
            return node;
        }
    }
}