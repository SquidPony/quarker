package net.slashie.libjcsi.textcomponents;

import java.util.ArrayList;
import java.util.List;
import net.slashie.libjcsi.CSIColor;
import net.slashie.libjcsi.wswing.WSwingConsoleInterface;

public class TextBox {
    protected final WSwingConsoleInterface console;
    protected int x;
    protected int y;
    protected int width = 20;
    protected int height = 5;
    protected boolean border = false;
    protected String title = "";
    protected String text = "";
    protected CSIColor foreColor = CSIColor.WHITE;
    protected CSIColor backColor = CSIColor.BLACK;

    public TextBox(WSwingConsoleInterface console) {
        this.console = console;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = Math.max(1, width);
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = Math.max(1, height);
    }

    public int getHeight() {
        return height;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public void setForeColor(CSIColor color) {
        if (color != null) {
            this.foreColor = color;
        }
    }

    public void clear() {
        setText("");
        draw();
    }

    public void draw() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                console.print(x + col, y + row, ' ', foreColor, backColor);
            }
        }

        int bodyX = x;
        int bodyY = y;
        int bodyW = width;
        int bodyH = height;

        if (border && width > 1 && height > 1) {
            drawBorder();
            bodyX++;
            bodyY++;
            bodyW -= 2;
            bodyH -= 2;
            if (!title.isEmpty()) {
                writeLine(x + 2, y, title, width - 3);
            }
        }

        List<String> lines = wrapText(text, bodyW);
        for (int i = 0; i < Math.min(bodyH, lines.size()); i++) {
            writeLine(bodyX, bodyY + i, lines.get(i), bodyW);
        }
    }

    protected void writeLine(int sx, int sy, String line, int max) {
        String out = line == null ? "" : line;
        for (int i = 0; i < max; i++) {
            char ch = i < out.length() ? out.charAt(i) : ' ';
            console.print(sx + i, sy, ch, foreColor, backColor);
        }
    }

    private void drawBorder() {
        for (int col = 0; col < width; col++) {
            console.print(x + col, y, '-', foreColor, backColor);
            console.print(x + col, y + height - 1, '-', foreColor, backColor);
        }
        for (int row = 0; row < height; row++) {
            console.print(x, y + row, '|', foreColor, backColor);
            console.print(x + width - 1, y + row, '|', foreColor, backColor);
        }
        console.print(x, y, '+', foreColor, backColor);
        console.print(x + width - 1, y, '+', foreColor, backColor);
        console.print(x, y + height - 1, '+', foreColor, backColor);
        console.print(x + width - 1, y + height - 1, '+', foreColor, backColor);
    }

    protected List<String> wrapText(String value, int width) {
        List<String> lines = new ArrayList<String>();
        if (width <= 0) {
            lines.add("");
            return lines;
        }
        if (value == null || value.isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] chunks = value.split(" XXX ");
        for (String chunk : chunks) {
            String remaining = chunk == null ? "" : chunk;
            while (remaining.length() > width) {
                lines.add(remaining.substring(0, width));
                remaining = remaining.substring(width);
            }
            lines.add(remaining);
        }
        return lines;
    }
}