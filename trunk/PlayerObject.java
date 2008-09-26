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
public class PlayerObject extends BaseObject {
    private int mass = 30;
    private int maxHp = 40;
    private int size = 1;
    private int level = 1;
    private int nextSize = 8;
    private int viewRange = 5;

    public PlayerObject() {
        super("Player", '@', true, CSIColor.WHITE);
    }

    @Override
    public String outputObjectToFile() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "GameObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + frontColor.getColor() + eol + String.valueOf(visible) + eol + getMass() + eol + getMaxHp() + eol + getSize() + eol + getLevel() + eol + getNextSize() + eol + eol;
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
            setMass((int) Integer.valueOf(reader.readLine()));
            setMaxHp((int) Integer.valueOf(reader.readLine()));
            setSize((int) Integer.valueOf(reader.readLine()));
            setLevel((int) Integer.valueOf(reader.readLine()));
            setNextSize((int) Integer.valueOf(reader.readLine()));
        } catch (IOException ioe) {
            System.out.println("Fatal error reading from file!");
            ioe.printStackTrace();
            return;
        }
    }

    public int getMass() {
        return mass;
    }

    public void setMass(int mass) {
        this.mass = mass;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getNextSize() {
        return nextSize;
    }

    public void setNextSize(int nextSize) {
        this.nextSize = nextSize;
    }

    public int getViewRange() {
        return viewRange;
    }

    public void setViewRange(int viewRange) {
        this.viewRange = viewRange;
    }
}