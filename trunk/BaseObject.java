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

    String myName;
    char represent;
    boolean passable;
    CSIColor frontColor;
    CSIColor backColor;
    boolean visible = false;
    static final CSIColor DEFAULT_FRONT_COLOR = CSIColor.WHITE,  DEFAULT_BACK_COLOR = CSIColor.BLACK;

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

    /*
     *This should be overridden by inheriting classes in order to provide additional output 
     * to the save file
     */
    String additionalOutput() {
        return "";
    }

    /*
     *This should be overridden by inheriting classes in order to provide additional output 
     * to the save file
     */
    String classNameOutput() {
        return "GameObject";
    }

    public String outputObjectToFile() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = classNameOutput() + eol + myName + eol + represent + eol + String.valueOf(passable) + eol + frontColor.getColor() + eol + String.valueOf(visible) + eol + eol;
        ret = ret + additionalOutput() + eol;
        return ret;
    }

    /*
     *This should be overridden by inheriting classes in order to provide additional input 
     * from the save file
     */
    void additionalInput(BufferedReader reader) throws IOException {//doesn't do anything in the base class
    }

    public void inputObjectFromFile(BufferedReader reader) throws IOException {
        myName = reader.readLine();
        represent = reader.readLine().charAt(0);
        passable = reader.readLine().equalsIgnoreCase("true");
        frontColor = new CSIColor(Integer.valueOf(reader.readLine()));
        visible = reader.readLine().equalsIgnoreCase("true");
        additionalInput(reader);
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        if ((name == null) || (name.isEmpty())) {
            return;
        }
        myName = name;
    }

    public char getRepresentation() {
        return represent;
    }

    public void setRepresentation(char rep) {
        if (rep == ' ') {
            return;
        }
        represent = rep;
    }

    public boolean isPassable() {
        return passable;
    }

    public void setPassable() {
        setPassable(true);
    }

    public void setPassable(boolean val) {
        passable = val;
    }

    public CSIColor getFrontColor() {
        return frontColor;
    }

    public void setFrontColor(CSIColor color) {
        frontColor = color;
    }

    public CSIColor getBackColor() {
        return backColor;
    }

    public void setBackColor(CSIColor color) {
        backColor = color;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible() {
        setVisible(true);
    }

    public void setVisible(boolean val) {
        visible = val;
    }

    public boolean equals(BaseObject obj) {
        return ((myName.equals(obj.myName)) && (represent == obj.getRepresentation()) &&
            (passable == obj.isPassable()) && (frontColor.equals(obj.getFrontColor())) &&
            backColor.equals(obj.getBackColor()) && (visible == obj.isVisible()));
    }
}

