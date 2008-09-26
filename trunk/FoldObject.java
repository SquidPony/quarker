/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

import java.io.*;
import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author ehoward
 */
public class FoldObject extends WallObject {

    public FoldObject(String name, char represent, boolean passable, CSIColor myColor) {
        super(name, represent, passable, myColor);
    }

    public FoldObject() {
        this(CSIColor.BLUE);
    }

    public FoldObject(CSIColor myColor) {
        super("spacetime fold", '#', false, myColor);
    }

    @Override
    public String outputObjectToFile() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "FoldObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + frontColor.getColor() + eol + String.valueOf(visible) + eol + eol;
        return ret;
    }

    @Override
    public void inputObjectFromFile(BufferedReader reader) {
        try {
            myName = reader.readLine();
            represent = reader.readLine().charAt(0);
            passable = reader.readLine().equalsIgnoreCase("true");
            frontColor = new CSIColor(Integer.valueOf(reader.readLine()));
            visible = reader.readLine().equalsIgnoreCase("true");
        } catch (IOException ioe) {
            System.out.println("Fatal error reading from file!");
            ioe.printStackTrace();
            return;
        }
    }
}
