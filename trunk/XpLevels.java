/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

import java.io.*;
import java.util.LinkedList;

/**
 *
 * @author ehoward
 */
public class XpLevels {

    public LinkedList<Integer> levels = new LinkedList<Integer>();

    public XpLevels() {
        int x = 0;
        for (int i = 1; i < 50; i++) {
            x += (i * i * i) + 15;  //xp formula
            levels.add((Integer) x);
        }
    }

    public void pushObject(BufferedReader reader) {
        String currentString = "";

        levels.clear();
        try {
            currentString = reader.readLine();
            while (!currentString.equalsIgnoreCase("")) {
                levels.add(Integer.valueOf(currentString));
                currentString = reader.readLine();
            }
        } catch (IOException ioe) {
            System.out.println("Fatal error reading from file!");
            ioe.printStackTrace();
            return;
        }
    }
}