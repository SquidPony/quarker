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
public class WallObject extends TerrainObject {

    public WallObject() {
        super("wall", '#', false);
    }

    public WallObject(String name, char represent, boolean passable, CSIColor myColor) {
        super(name, represent, passable, myColor);
    }

    public WallObject(CSIColor myColor) {
        super("wall", '#', false, myColor);
    }
}
