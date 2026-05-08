package my.quarker;

import net.slashie.libjcsi.CSIColor;
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

    public void deepCopy(TerrainObject obj){
        deepCopy((BaseObject)obj);
        everSeen = obj.everSeen;
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
