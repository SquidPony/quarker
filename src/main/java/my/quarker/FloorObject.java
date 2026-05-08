package my.quarker;

import net.slashie.libjcsi.CSIColor;
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

    public void deepCopy(FloorObject obj){
        deepCopy((TerrainObject)obj);
    }

    @Override
    public String outputObjectToFile() {
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "FloorObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + frontColor.getColor() + eol + String.valueOf(visible) + eol + eol;
        return ret;
    }
}
