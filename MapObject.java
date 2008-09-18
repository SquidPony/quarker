package my.quarker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author ehoward
 */
public class MapObject {

    private BaseObject flooring; //should be either a wall or floor or stairs
    private MonsterObject monster;
    private ArrayList<ItemObject> items;

    public MapObject() {
        this(DebugObject.DEFAULT, null);
    }

    public MapObject(WallObject wall) {
        this(wall, null);
    }

    public MapObject(TerrainObject floor) {
        this(floor, null);
    }

    public MapObject(BaseObject base, MonsterObject monster) {
        flooring = base;
        this.monster = monster;
        items = null;
    }

    public MapObject(BaseObject base, MonsterObject monster, ItemObject... itemList) {
        this(base, monster);
        items = new ArrayList<ItemObject>(itemList.length);
        for (int i = 0; i < itemList.length; i++) {
            items.add(itemList[i]);
        }
    }

    public void setFlooring(BaseObject floor) {
        flooring = floor;
    }

    public void setFlooring() {
        flooring = FloorObject.DEFAULT_FLOOR;
    }

    public void setMonster(MonsterObject mon) {
        monster = mon;
    }

    public MonsterObject getMonster() {
        return monster;
    }

    public boolean hasMonster() {
        return !(monster == null);
    }

    public boolean hasItem() {
        if (items == null) {
            return false;
        }
        if (items.isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean isFloor() {
        return (flooring instanceof FloorObject);
    }

    public boolean isStairs() {
        return (flooring instanceof StairsObject);
    }

    public boolean isWall() {
        return (flooring instanceof WallObject);
    }

    public BaseObject getFlooring() {
        return flooring;
    }

    public BaseObject getTopObject() {
        if (hasMonster()) {
            return monster;
        } else if (hasItem()) {
            return items.get(0);
        }
        return flooring;
    }

    public String getTopObjectName() {
        Object obj = getTopObject();
        if (obj instanceof BaseObject) {
            BaseObject tempObj = (BaseObject) obj;
            return tempObj.myName;
        }
        if (obj instanceof ItemObject) {
            return "an item";
        }
        return "nothing";
    }

    public boolean isPassable() {
        if (!flooring.passable) {
            return false;
        } else if (hasMonster()) {
            if (!monster.passable) {
                return false;
            }
        }
        return true;
    }

    public boolean isVisible() {
        if (!flooring.visible) {
            return false;
        } else if (hasMonster()) {
            if (!monster.visible) {
                return false;
            }
        }
        return true;
    }
    
    public void setVisible(){
        setVisible(true);
    }
    
    public void setVisible(boolean vis){
        flooring.visible = vis;
        if (hasMonster()){
            monster.visible = vis;
        }
    }

    public void objectOutput(BufferedWriter writer) throws IOException {
        writer.write(flooring.objectOutput());
        if (hasMonster()) {
            writer.write(monster.objectOutput());
        }
        if (hasItem()) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).objectOutput();
            }
        }
    }

    public void resetMe() {
        flooring = DebugObject.DEFAULT;
        monster = null;
        items = null;
    }

    public String pushObject(BufferedReader reader, String type) throws IOException {

        resetMe();
        String currentType = type;

        if (currentType.equalsIgnoreCase("FoldObject")) {
            flooring = new FoldObject();
        } else if (currentType.equalsIgnoreCase("DebugObject")) {
            flooring = new DebugObject();
        } else if (currentType.equalsIgnoreCase("FloorObject")) {
            flooring = new FloorObject();
        } else if (currentType.equalsIgnoreCase("NullObject")) {
            flooring = new NullObject();
        } else if (currentType.equalsIgnoreCase("StairsObject")) {
            flooring = new StairsObject();
        } else if (currentType.equalsIgnoreCase("WallObject")) {
            flooring = new WallObject();
        } else {
            throw new IOException("Object not known.");
        }

        flooring.pushObject(reader);
        reader.readLine(); //gets rid of readability space between entries
        currentType = reader.readLine();
        
        if (currentType.equals("MonsterObject")) {
            monster = new MonsterObject();
            monster.pushObject(reader);
            reader.readLine(); //gets rid of readability space between entries
            currentType = reader.readLine();
        }

        if (currentType.equals("ItemObject")) {
            items = new ArrayList<ItemObject>();
            ItemObject tempItem;
            do {
                tempItem = new ItemObject();
                tempItem.pushObject(reader);
                items.add(tempItem);
                reader.readLine(); //gets rid of readability space between entries
                currentType = reader.readLine();
            } while (currentType.equals("ItemObject"));
        }

        return currentType;

    }
}
