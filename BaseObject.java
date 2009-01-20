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
public class BaseObject implements Cloneable{

    protected String myName;
    protected char represent;
    protected boolean passable;
    protected CSIColor frontColor, backColor;
    protected double visible = 0;
    protected static final CSIColor DEFAULT_FRONT_COLOR = CSIColor.WHITE,  DEFAULT_BACK_COLOR = CSIColor.BLACK;
    protected int sizeUsed;
    protected static final int MINIMUM = 1, SMALL = 25, MEDIUM = 50, LARGE = 75, MAXIMUM = 100; //guidlines for object size in spaces

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
        sizeUsed = MEDIUM;
    }

    public void deepCopy(BaseObject obj){
        this.myName = obj.myName;
        this.represent = obj.represent;
        this.passable = obj.passable;
        this.frontColor = obj.frontColor;
        this.backColor = obj.backColor;
        this.sizeUsed = obj.sizeUsed;
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
        visible = Double.valueOf(reader.readLine());
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
        return (visible > 0);
    }

    public void setVisible(double val) {
        visible = val;
    }
    
    public Double getVisible(){
        return visible;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public char getRepresent() {
        return represent;
    }

    public void setRepresent(char represent) {
        this.represent = represent;
    }

    public int getSizeUsed() {
        return sizeUsed;
    }

    public void setSizeUsed(int sizeUsed) {
        this.sizeUsed = sizeUsed;
    }

    public boolean equals(BaseObject obj) {
        return ((myName.equals(obj.myName)) && (represent == obj.getRepresentation()) &&
            (passable == obj.isPassable()) && (frontColor.equals(obj.getFrontColor())) &&
            backColor.equals(obj.getBackColor()) && (visible == obj.getVisible()));
    }
}

