package my.quarker;

import net.slashie.libjcsi.CSIColor;
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

    public void deepCopy(WallObject obj){
        deepCopy((TerrainObject)obj);
    }
}
