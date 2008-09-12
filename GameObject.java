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
public class GameObject {

    public String myName;
    public char represent;
    public boolean passable;
    public CSIColor myColor;
    public CSIColor backColor;
    private static final CSIColor defaultColor = CSIColor.WHITE;
    private static final CSIColor defaultBackColor = CSIColor.BLACK;
    public boolean visible = false;

    public GameObject() { //default is creating a basic wall
        this("unknown", '?', false, defaultColor);
    }

    public GameObject(String name, char represent, boolean passable) {
        this(name, represent, passable, defaultColor);
    }

    public GameObject(String name, char represent, boolean passable, CSIColor myColor) {
        this(name, represent, passable, myColor, defaultBackColor);
    }
    
       public GameObject(String name, char represent, boolean passable, CSIColor myColor, CSIColor backColor) {
        this.myName = name;
        this.represent = represent;
        this.passable = passable;
        this.myColor = myColor;
        this.backColor = backColor;
    }

    public String objectOutput() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "GameObject" + eol + myName + eol + represent + eol + String.valueOf(passable) + eol + myColor.getColor() + eol + String.valueOf(visible) + eol + eol;
        return ret;
    }

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

