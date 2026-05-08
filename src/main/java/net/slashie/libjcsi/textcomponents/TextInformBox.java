package net.slashie.libjcsi.textcomponents;

import java.util.ArrayList;
import java.util.List;
import net.slashie.libjcsi.wswing.WSwingConsoleInterface;

public class TextInformBox extends TextBox {
    private final List<String> history = new ArrayList<String>();

    public TextInformBox(WSwingConsoleInterface console) {
        super(console);
        setBorder(false);
    }

    public void addText(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        history.add(value);
        while (history.size() > Math.max(1, getHeight())) {
            history.remove(0);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            if (i > 0) {
                sb.append(" XXX ");
            }
            sb.append(history.get(i));
        }
        setText(sb.toString());
    }

    @Override
    public void clear() {
        history.clear();
        setText("");
        draw();
    }
}