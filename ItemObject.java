package my.quarker;

import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author ehoward
 */
public class ItemObject extends BaseObject {

    int quantity;

    public ItemObject() {
        super("Item", '*', true, CSIColor.VEGAS_GOLD);
        quantity = 1;
    }
    
    public ItemObject(String name, char rep, boolean pass, CSIColor color, int quant){
        super(name, rep, pass, color);
        quantity = quant;
    }

    public void setQuantity(int num) {
        if (num >= 0) {
            quantity = num;
        } else {
            quantity = 0;
        }
    }

    public void deepCopy(ItemObject obj){
        deepCopy((BaseObject)obj);
        quantity = obj.quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}
