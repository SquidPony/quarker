/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

import java.io.*;
import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author Eben
 */
public class TerrainObject extends BaseObject {

    public static final TerrainObject DEFAULT = new TerrainObject();
    
    public TerrainObject() {
        super("terrain", '~', true);
    }
    
    public TerrainObject(String name, char represent, boolean passable){
        super(name, represent, passable);
    }
    
    public TerrainObject(String name, char represent, boolean passable, CSIColor color){
        super(name, represent, passable, color);
    }

    @Override
    public String objectOutput() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "TerrainObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + myColor.getColor() + eol + String.valueOf(visible) + eol + eol;
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
        } catch (IOException ioe) {
            System.out.println("Fatal error reading from file!");
            ioe.printStackTrace();
            return;
        }
    }
}
