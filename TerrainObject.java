/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

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
