package my.quarker;

import java.io.*;
import net.slashie.libjcsi.CSIColor;

public class StairsObject extends GameObject {

    boolean down = true; //if it's an up stair, this will be false

    public StairsObject() {
        super("stairs", '>', true);
    }

    public StairsObject(boolean goingDown) {
        if (goingDown) {
            myName = "stairs leading downward";
            represent = '>';
            passable = true;
        } else {
            myName = "stairs leading upward";
            represent = '<';
            passable = true;
            down = false;
        }
    }

    @Override
    public String objectOutput() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "StairsObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + myColor.getColor() + eol + String.valueOf(visible) + eol + String.valueOf(down) + eol + eol;
        return ret;
    }

    @Override
    public void pushObject(BufferedReader reader) {
        try {
            myName = reader.readLine();
            represent = reader.readLine().charAt(0);
            passable = reader.readLine().equalsIgnoreCase("true");
            myColor = new CSIColor(Integer.valueOf(reader.readLine()));
            visible = reader.readLine().equalsIgnoreCase("true");
            down = reader.readLine().equalsIgnoreCase("true");
        } catch (IOException ioe) {
            System.out.println("Fatal error reading from file!");
            ioe.printStackTrace();
            return;
        }
    }
}
