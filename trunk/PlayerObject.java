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
public class PlayerObject extends GameObject {

    public int hp = 30;
    public int maxHp = 40;
    public int size = 5;
    public int level = 1;
    public int nextSize = 8;

    public PlayerObject() {
        super("Player", '@', true, CSIColor.WHITE);
    }

    @Override
    public String objectOutput() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "GameObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + myColor.getColor() + eol + String.valueOf(visible) + eol + hp + eol + maxHp + eol + size + eol + level + eol + nextSize + eol + eol;
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
            hp = Integer.valueOf(reader.readLine());
            maxHp = Integer.valueOf(reader.readLine());
            size = Integer.valueOf(reader.readLine());
            level = Integer.valueOf(reader.readLine());
            nextSize = Integer.valueOf(reader.readLine());
        } catch (IOException ioe) {
            System.out.println("Fatal error reading from file!");
            ioe.printStackTrace();
            return;
        }
    }
}