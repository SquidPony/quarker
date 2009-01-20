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
public class FoldObject extends WallObject {

    public FoldObject(String name, char represent, boolean passable, CSIColor myColor) {
        super(name, represent, passable, myColor);
    }

    public FoldObject() {
        this(CSIColor.BLUE);
    }

    public FoldObject(CSIColor myColor) {
        super("spacetime fold", '#', false, myColor);
    }

    public void deepCopy(FoldObject obj){
        deepCopy((WallObject)obj);
    }
}
