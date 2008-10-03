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

    private TerrainObject flooring; //should be either a wall or floor or stairs
    private MonsterObject monster;
    private ArrayList<ItemObject> items;
    private Boolean changed = true;

    public MapObject() {
    }

    public MapObject(TerrainObject floor) {
        this(floor, null);
    }

    public MapObject(TerrainObject base, MonsterObject monster) {
        flooring = base;
        this.monster = monster;
        items = null;
    }

    public MapObject(TerrainObject base, MonsterObject monster, ItemObject... itemList) {
        this(base, monster);
        items = new ArrayList<ItemObject>(itemList.length);
        for (int i = 0; i < itemList.length; i++) {
            items.add(itemList[i]);
        }
    }
    
    public boolean hasBeenSeen(){
        return flooring.isEverSeen();
    }
    
    public void setHasBeenSeen(){
        setHasBeenSeen(true);
    }
    
    public void setHasBeenSeen(boolean seen){
        flooring.setEverSeen(seen);
    }

    public boolean isChanged(){
        return changed;
    }
    
    public void setChanged(boolean change){
        changed = change;
    }
    
    public void setChanged(){
        setChanged(true);
    }
    
    public void setFlooring(TerrainObject floor) {
        flooring = floor;
    }

    public void setFlooring() {
        flooring = new FloorObject();
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
            return tempObj.getName();
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
        if (flooring.visible == 0) {
            return false;
        } else if (hasMonster()) {
            if (monster.visible == 0) {
                return false;
            }
        }
        return true;
    }
    
    public void setVisible(double vis){
        flooring.visible = vis;
        if (hasMonster()){
            monster.visible = vis;
        }
    }
    
    public double getVisible(){
        return Math.max(Math.max(0, monster.getVisible()), flooring.getVisible());
    }

    public void objectOutput(BufferedWriter writer) throws IOException {
        writer.write(flooring.outputObjectToFile());
        if (hasMonster()) {
            writer.write(monster.outputObjectToFile());
        }
        if (hasItem()) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).outputObjectToFile();
            }
        }
    }

    public void resetMe() {
        flooring = null;
        monster = null;
        items = null;
    }

    public String pushObject(BufferedReader reader, String type) throws IOException {

        resetMe();
        String currentType = type;

        if (currentType.equalsIgnoreCase("FoldObject")) {
            flooring = new FoldObject();
        } else if (currentType.equalsIgnoreCase("FloorObject")) {
            flooring = new FloorObject();
        } else if (currentType.equalsIgnoreCase("StairsObject")) {
            flooring = new StairsObject();
        } else if (currentType.equalsIgnoreCase("WallObject")) {
            flooring = new WallObject();
        } else {
            throw new IOException("Object not known.");
        }

        flooring.inputObjectFromFile(reader);
        reader.readLine(); //gets rid of readability space between entries
        currentType = reader.readLine();
        
        if (currentType.equals("MonsterObject")) {
            monster = new MonsterObject();
            monster.inputObjectFromFile(reader);
            reader.readLine(); //gets rid of readability space between entries
            currentType = reader.readLine();
        }

        if (currentType.equals("ItemObject")) {
            items = new ArrayList<ItemObject>();
            ItemObject tempItem;
            do {
                tempItem = new ItemObject();
                tempItem.inputObjectFromFile(reader);
                items.add(tempItem);
                reader.readLine(); //gets rid of readability space between entries
                currentType = reader.readLine();
            } while (currentType.equals("ItemObject"));
        }

        return currentType;

    }
}
