package my.quarker;

import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author Eben Howard
 */
public class GluonObject extends ItemObject {

    public GluonObject(){
        super("Gluon", '!', true, CSIColor.ATOMIC_TANGERINE, 1);
    }

    public void deepCopy(GluonObject obj){
        deepCopy((ItemObject)obj);
    }
}
