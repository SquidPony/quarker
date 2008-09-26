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
    boolean everSeen = false;

    public TerrainObject() {
        super("terrain", '~', true);
    }

    public TerrainObject(String name, char represent, boolean passable) {
        super(name, represent, passable);
    }

    public TerrainObject(String name, char represent, boolean passable, CSIColor color) {
        super(name, represent, passable, color);
    }

    @Override
    String additionalOutput() {
        return String.valueOf(everSeen);
    }

    @Override
    public String outputObjectToFile() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = super.outputObjectToFile();
        ret = ret + additionalOutput() + eol;
        return ret;
    }

    @Override
    void additionalInput( BufferedReader reader) throws IOException {
        everSeen = reader.readLine().equalsIgnoreCase("true");
    }

    @Override
    public void inputObjectFromFile(BufferedReader reader) throws IOException {
        myName = reader.readLine();
        represent = reader.readLine().charAt(0);
        passable = reader.readLine().equalsIgnoreCase("true");
        frontColor = new CSIColor(Integer.valueOf(reader.readLine()));
        visible = reader.readLine().equalsIgnoreCase("true");
        additionalInput(reader);
    }

    public boolean isEverSeen() {
        return everSeen;
    }

    public void setEverSeen() {
        setEverSeen(true);
    }

    public void setEverSeen(boolean everSeen) {
        this.everSeen = everSeen;
    }
}
