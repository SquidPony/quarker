/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

import java.io.*;
import net.slashie.libjcsi.*;

/**
 *
 * @author Eben
 */
public class BaseObject {

    public String myName;
    public char represent;
    public boolean passable;
    public CSIColor frontColor;
    public CSIColor backColor;
    protected static final CSIColor DEFAULT_FRONT_COLOR = CSIColor.WHITE, 
        DEFAULT_BACK_COLOR = CSIColor.BLACK;
    public boolean visible = false;

    public BaseObject() { //default is creating a basic wall
        this("unknown", '?', false, DEFAULT_FRONT_COLOR);
    }

    public BaseObject(String name, char represent, boolean passable) {
        this(name, represent, passable, DEFAULT_FRONT_COLOR);
    }

    public BaseObject(String name, char represent, boolean passable, CSIColor myColor) {
        this(name, represent, passable, myColor, DEFAULT_BACK_COLOR);
    }
    
       public BaseObject(String name, char represent, boolean passable, CSIColor myColor, CSIColor backColor) {
        this.myName = name;
        this.represent = represent;
        this.passable = passable;
        this.frontColor = myColor;
        this.backColor = backColor;
    }

    public String objectOutput() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "GameObject" + eol + myName + eol + represent + eol + String.valueOf(passable) + eol + frontColor.getColor() + eol + String.valueOf(visible) + eol + eol;
        return ret;
    }

    public void pushObject(BufferedReader reader) {
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

