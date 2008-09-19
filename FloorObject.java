/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package my.quarker;

import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author ehoward
 */
public class FloorObject extends TerrainObject{
    
    
    public static final FloorObject DEFAULT_FLOOR = new FloorObject();
    public FloorObject() {
        super("floor", '.', true);
    }
    
    public FloorObject(String name, char represent, boolean passable){
        super(name, represent, passable);
    }
    
    public FloorObject(String name, char represent, boolean passable, CSIColor color){
        super(name, represent, passable, color);
    }

    @Override
    public String objectOutput() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "FloorObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + frontColor.getColor() + eol + String.valueOf(visible) + eol + eol;
        return ret;
    }
}
