package net.slashie.libjcsi.textcomponents;

import net.slashie.libjcsi.wswing.WSwingConsoleInterface;

public class DialogBox extends TextBox {
    public DialogBox(WSwingConsoleInterface console, int lines, String text) {
        super(console);
        setBorder(true);
        setHeight(Math.max(3, lines + 4));
        int requestedWidth = Math.max(24, (text == null ? 0 : text.length()) + 4);
        setWidth(Math.min(console.xdim - 2, requestedWidth));
        setText(text);
    }
}