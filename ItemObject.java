package my.quarker;

/**
 *
 * @author ehoward
 */
public class ItemObject extends BaseObject {

    private int quantity;

    public ItemObject() {

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
