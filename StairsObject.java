package my.quarker;

public class StairsObject extends TerrainObject {

    boolean down = true; //if it's an up stair, this will be false

    public StairsObject() {
        this(true);
    }

    public StairsObject(boolean goingDown) {
        passable = true;
        visible = 100;
        if (goingDown) {
            myName = "spacetime advancing wormhole";
            represent = '>';
            down = true;
        } else {
            myName = "spacetime retracting wormhole";
            represent = '<';
            down = false;
        }
    }

    public void deepCopy(StairsObject obj){
        deepCopy((TerrainObject)obj);
        down = obj.down;
    }
}
