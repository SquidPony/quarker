package my.quarker;

import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author ehoward
 */
public class ItemObject extends BaseObject {

    private int quantity;

    public ItemObject() {
        super("Item", '*', true, CSIColor.VEGAS_GOLD);
        quantity = 1;
    }

    public void setQuantity(int num) {
        if (num >= 0) {
            quantity = num;
        } else {
            quantity = 0;
        }
    }

    public int getQuantity() {
        return quantity;
    }
}
