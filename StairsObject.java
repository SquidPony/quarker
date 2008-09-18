package my.quarker;

import java.io.*;
import net.slashie.libjcsi.CSIColor;

public class StairsObject extends TerrainObject {

    boolean down = true; //if it's an up stair, this will be false

    public StairsObject() {
        this(true);
    }

    public StairsObject(boolean goingDown) {
        passable = true;
        visible = true;
        if (goingDown) {
            myName = "spacetime advancing wormhole";
            represent = '>';
            down = true;
        } else {
            myName = "spacetime retracting wormhole";
            represent = '<';
            down = false;
        }
    }

    @Override
    public String objectOutput() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "StairsObject" + eol + 
            myName + eol + 
            String.valueOf(represent) + eol + 
            String.valueOf(passable) + eol + 
            myColor.getColor() + eol + 
            String.valueOf(visible) + eol + 
            String.valueOf(down) + eol;//for some unknown reason, the last line being read doesn't get stripped so this is a kludge (removing the usual trailing + eol) to fix that bug.  I must figure out why it's happening and stop it though!
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