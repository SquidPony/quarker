package my.quarker;

import java.awt.Point;
import java.io.*;
import java.util.Random;
import net.slashie.libjcsi.*;
import net.slashie.libjcsi.textcomponents.*;
import net.slashie.libjcsi.wswing.*;
import net.slashie.util.FileUtil;

public class Workhorse {
    //-------------------Custom variables-------------------//
    
    private String versionNumber = "0.03d";
    
    private WSwingConsoleInterface mainInterface;
    private TextInformBox infoBox;
    private int infoSpace = 2; //this is the height of the infoBox
    private TextBox statsBox;
    private Random rng = new Random();
    private int mapSizeX = 65;//don't forget that the edges will all be walls
    private int mapSizeY = 23;
    private int mapLevel = 1; //depth of the dungeon, starts at 1
    private int recurseNumber = 0; //this will allow us to limit the number of recurses on recursive methods
    private int maxRecurse = 150; //this is the most recurses allowed (likely to be changed right before calling a recursive function
    private GameObject[][] mapContents = new GameObject[mapSizeX][mapSizeY];
    private Point currentLoc = new Point((mapSizeX / 2), (mapSizeY / 2));
    private XpLevels quarkLevels = new XpLevels();
    private PlayerObject player = new PlayerObject();
    
    private String eol = System.getProperty("line.separator");

    public Workhorse() {
        try {
            mainInterface = new WSwingConsoleInterface("Quarker", true);
        } catch (ExceptionInInitializerError eiie) {
            System.out.println("Fatal Error Initializing Swing Console Box");
            eiie.printStackTrace();
            System.exit(-1);
        }

        //--------for testing screen size-----//
//        int x =0;
//        for (int i = 0; i < mainInterface.xdim; i++){
//            for (int k = 0; k < mainInterface.ydim; k++){
//                x = k;
//                while (x > 9){
//                    x = (int)(i/10);
//                }
//                mainInterface.print(i,k,String.valueOf(x),Color.WHITE);
//            }
//        }
//        statsBox = new TextBox(mainInterface); 
//        statsBox.setPosition(0, 0);
//        statsBox.setWidth(mainInterface.xdim - 1);
//        statsBox.setHeight(mainInterface.ydim - 1);
//        statsBox.setBorder(true);
//        statsBox.clear();
//        statsBox.setTitle("Q");
//        statsBox.setText("This");
//        statsBox.draw();
        
        initEverything();
        mainInterface.refresh();
        getPlayerName();
        playerTurn();
    }

    private void playerTurn() {// this will get the player's input and launch the method that checks what was input
        CharKey actionKey = new CharKey();

        do {
            actionKey = mainInterface.inkey();
            infoBox.clear();
            TakeAction(actionKey);
        } while (true);
    }

    private void runTurn() { // here is where everything happens after the player does something which takes time
        runMonsterTurn();
        checkStats(); // this will see if the player has leveled up or maybe died.
        displayMap(); // this should most likey be the last thing that happens before the player gets to go again
        mainInterface.refresh();
    }

    private void TakeAction(CharKey thisKey) {  // this will feed control to the appropriate method based on player input
        if (thisKey.isArrow()) {
            if (thisKey.isUpArrow()) {
                tryToMove(currentLoc.x, currentLoc.y - 1);
            } else if (thisKey.isUpRightArrow()) {
                tryToMove(currentLoc.x + 1, currentLoc.y - 1);
            } else if (thisKey.isRightArrow()) {
                tryToMove(currentLoc.x + 1, currentLoc.y);
            } else if (thisKey.isDownRightArrow()) {
                tryToMove(currentLoc.x + 1, currentLoc.y + 1);
            } else if (thisKey.isDownArrow()) {
                tryToMove(currentLoc.x, currentLoc.y + 1);
            } else if (thisKey.isDownLeftArrow()) {
                tryToMove(currentLoc.x - 1, currentLoc.y + 1);
            } else if (thisKey.isLeftArrow()) {
                tryToMove(currentLoc.x - 1, currentLoc.y);
            } else if (thisKey.isUpLeftArrow()) {
                tryToMove(currentLoc.x - 1, currentLoc.y - 1);
            }
        } else {
            switch (thisKey.code) {
                case CharKey.ESC:
                    leaving();
                    break;
                case CharKey.MORETHAN:
                    tryToGoDownStairs();
                    break;
                case CharKey.LESSTHAN:
                    tryToGoUpStairs();
                    break;
                case CharKey.S:
                    saveGame();
                    break;
                case CharKey.R:
                    loadGame();
                    break;
            }
        }
    }

    private void runMonsterTurn() {


        int x, y;
        for (int i = 0; i < mapSizeX; i++) {
            for (int k = 0; k < mapSizeY; k++) {
                if ((mapContents[i][k] instanceof MonsterObject)) {
                    MonsterObject monObj = (MonsterObject) mapContents[i][k];
                    if (monObj.getWakeful()) {
                        x = 0;
                        y = 0;
                        if (i > (currentLoc.x + 1)) {
                            x = - 1;
                        }
                        if (i < (currentLoc.x) - 1) {
                            x = +1;
                        }
                        if (k > (currentLoc.y + 1)) {
                            y = - 1;
                        }
                        if (k < (currentLoc.y) - 1) {
                            y = +1;
                        }
                        if ((x == 0) && (y == 0)) {
                            monsterAttacks(i, k);
                        } else {
                            if (mapContents[i + x][k + y] instanceof FloorObject) {
                                mapContents[i][k] = new FloorObject();
                                mapContents[i + x][k + y] = monObj;
                            } else {
                                tellPlayer("The " + monObj.myName + " glares at the " + mapContents[i + x][k + y].myName + " that's in its way.");
                            }
                        }
                    }
                }
            }
        }
    }

    private void monsterAttacks(int a, int b) {
        MonsterObject monster = (MonsterObject) mapContents[a][b];
        int attack;
        attack = monster.getAttack();
        if ((attack + rng.nextInt(10)) > (player.size + rng.nextInt(30))) {
            player.hp -= monster.getDamage();
            tellPlayer("The " + monster.myName + " did " + String.valueOf(monster.getDamage()) + " points of damage to you!");
        } else {
            tellPlayer("The " + monster.myName + " missed you.");
        }
    }

    private void levelUp() {
        player.nextSize = (int) quarkLevels.levels.removeFirst();
        player.level++;
        tellPlayer("You have grown larger!");
    }

    private void leaving() {
        tellPlayer("Thanks for playing.  Press any key to exit now.");
        mainInterface.inkey();
        System.exit(0);
    }

    private void checkStats() {
        if (player.hp < 1) {
            tellPlayer("You have died.");
            leaving();

        }
        if (player.size >= player.nextSize) {
            levelUp();
        }
    }

    private void tryToGoDownStairs() {
        if (mapContents[currentLoc.x][currentLoc.y] instanceof StairsObject) {
            StairsObject nowObj = (StairsObject) mapContents[currentLoc.x][currentLoc.y];
            if (nowObj.down) {
                buildNewLevel(true);
            } else {
                tellPlayer("These stairs don't lead down.");
            }
        } else {
            tellPlayer("You don't see any stairs here.");
        }
    }

    private void tryToGoUpStairs() {
        if (mapContents[currentLoc.x][currentLoc.y] instanceof StairsObject) {
            StairsObject nowObj = (StairsObject) mapContents[currentLoc.x][currentLoc.y];
            if (!(nowObj.down)) {
                buildNewLevel(false);
            } else {
                tellPlayer("These stairs don't lead up.");
            }
        } else {
            tellPlayer("You don't see any stairs here.");
        }
    }

    private void tryToMove(int a, int b) {
        GameObject nowObj = mapContents[a][b];

        if (nowObj.passable) {
            currentLoc.move(a, b);
        } else if (nowObj instanceof WallObject) {
        }

        if (mapContents[a][b] instanceof MonsterObject) {
            if (doFight((MonsterObject) mapContents[a][b])) {
                mapContents[a][b] = new FloorObject();
            }
        } else if (!(mapContents[a][b].passable)) {
            tellPlayer("You can't walk through the " + mapContents[a][b].myName + "!");
        } else if (mapContents[a][b].passable) {
            currentLoc.move(a, b);
        }
        runTurn();
    }

    public boolean doFight(MonsterObject monster) {


        int r, i;

        r = rng.nextInt(100);
        r = r + player.size - monster.getDefense();

        monster.wakeUp(); //attacking will cause the monster to wake up, even if not hit

        if (r < 50) {
            //miss
            tellPlayer("You missed!");
            return false;
        } else {
            i = rng.nextInt((player.size / 2) + player.size);
            if (i <= 0) {
                tellPlayer("You hit the " + monster.myName + ", but didn't hurt it!");
            } else {
                tellPlayer("You hit the " + monster.myName + " for " + i + " damage.");
                monster.applyDamage(i);
            }

            if (monster.getHp() < 0) {//swallow!
                tellPlayer("You absorbed the " + monster.myName + "!");
                player.size += monster.getSatiation();
                return true;
            } else {
                return false;
            }
        }
    }

    private void displayMap() {

        String beol =" XXX ";//end of line for TextBox
        GameObject nowContents;
        for (int k = 0; k < mapSizeX; k++) {
            for (int i = 0; i < mapSizeY; i++) {
                nowContents = mapContents[k][i];
                mainInterface.print(k, i + 2, nowContents.represent, nowContents.myColor);
            }
        }
        mainInterface.print(currentLoc.x, currentLoc.y + infoSpace, player.represent, player.myColor);
        infoBox.draw();
        statsBox.setText(
            beol
            + "Health: " + player.hp + beol
            + "Size: " + player.level + beol
            + "Xp: " + player.size + beol
            + "Depth: " + mapLevel + beol
            );
        statsBox.draw();
        mainInterface.refresh();
    }

    private void buildDisplay() {
        mainInterface.cls();
        infoBox = new TextInformBox(mainInterface);
        infoBox.setPosition(0, 0);
        infoBox.setWidth(mapSizeX - 1);
        infoBox.setHeight(2);
        infoBox.setText("");
        infoBox.draw();
        statsBox = new TextBox(mainInterface);
//        statsBox.setBounds(mapSizeX + 1, 0, (mainInterface.xdim - mapSizeX - 1), mainInterface.ydim);
        statsBox.setPosition(mapSizeX, 0);
        statsBox.setWidth(mainInterface.xdim - mapSizeX - 1);
        statsBox.setHeight(mainInterface.ydim - 1);
        statsBox.setBorder(true);
        statsBox.clear();
        statsBox.setTitle("Q" + " V" + versionNumber);
        statsBox.draw();
    }

    public static void main(String[] args) {
        new Workhorse();
    }

    private void chaosMapping(Point start) { //this will recursively make a random cloud
        if (recurseNumber > maxRecurse) {
            return;
        }

        int i = start.x;
        int k = start.y;
        if (!((i > (mapSizeX - 1)) || (k > (mapSizeY - 1)) || (i < 0) || (k < 0))) { //make sure we're not over the edge of the map
            mapContents[i][k] = new CloudObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30)));
        }
        recurseNumber++;
        i = i + 1 - rng.nextInt(3);
        k = k + 1 - rng.nextInt(3);
        chaosMapping(new Point(i, k));
    }

    private void creaturePlacement() {// this will place creatures randomly
        int r; //this will hold our random numbers
        //let's fill in the rest randomly for now
        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < (mapSizeY); k++) {
                if (!(mapContents[i][k] instanceof WallObject)) {
                    r = rng.nextInt(25); //about 1/25th of the tiles will have an enemy
                    if (r == 0) {
                        if (mapLevel < 3) {
                            r = rng.nextInt(750) + 250;
                        } else if (mapLevel < 5) {
                            r = rng.nextInt(300) + 10;
                        } else {
                            r = rng.nextInt(75);
                        }
                        if (r < 15) {
                            mapContents[i][k] = new MonsterObject(MonsterTypeEnum.TRUTH);
                        } else if (r < 40) {
                            mapContents[i][k] = new MonsterObject(MonsterTypeEnum.BEAUTY);
                        } else if (r < 100) {
                            mapContents[i][k] = new MonsterObject(MonsterTypeEnum.CHARM);
                        } else if (r < 300) {
                            mapContents[i][k] = new MonsterObject(MonsterTypeEnum.STRANGE);
                        } else if (r < 600) {
                            mapContents[i][k] = new MonsterObject(MonsterTypeEnum.UP);
                        } else {
                            mapContents[i][k] = new MonsterObject(MonsterTypeEnum.DOWN);
                        }
                    } //else {mapContents[i][k] = new FloorObject();}
                }
            }
        }
    }

    private void floorPlacement() {

        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < (mapSizeY); k++) {
                if (!(mapContents[i][k] instanceof WallObject)) {
                    mapContents[i][k] = new FloorObject();
                }
            }
        }
    }

    private void floodFill(int x, int y) {
        if ((x < 0) || (x > (mapSizeX - 1)) || (y < 0) || (y > (mapSizeY - 1))) {
            return; //exits if coords are out of range for the map
        }
        if (mapContents[x][y].visible) {
            return;
        }
        if (!(mapContents[x][y] instanceof WallObject)) {
            mapContents[x][y].visible = true;
            for (int i = -1; i < 2; i++) {
                for (int k = -1; k < 2; k++) {
                    floodFill(x + i, y + k);
                }
            }
        }
    }

    private void crushWalls(int x, int y) {
        do {
            if (x > currentLoc.x) {
                x--;
            }
            if (x < currentLoc.x) {
                x++;
            }
            if (y > currentLoc.y) {
                y--;
            }
            if (y < currentLoc.y) {
                y++;
            }
            if (mapContents[x][y] instanceof WallObject) {
                mapContents[x][y] = new FloorObject();
            }
        } while (!(mapContents[x][y].visible));

    }

    private void checkMapForConnectivity() {

        floodFill(currentLoc.x, currentLoc.y);

        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < (mapSizeY); k++) {
                if (!(mapContents[i][k] instanceof WallObject) && !(mapContents[i][k].visible)) {
                    crushWalls(i, k);
                    floodFill(i, k);
                }
            }
        }
    }

    private void buildNewLevel(boolean goingDown) {
        currentLoc.x = rng.nextInt(mapSizeX - 2) + 1;
        currentLoc.y = rng.nextInt(mapSizeY - 2) + 1;
        if (goingDown) {
            mapLevel++;
            buildMap();
            mapContents[currentLoc.x][currentLoc.y] = new StairsObject(false);
            tellPlayer("You have decended the stairs.");
        } else {
            mapLevel--;
            buildMap();
            tellPlayer("The stairs collapse behind you.  Looks like you'll have to find another way down.");
        }
        displayMap();
    }

    private void tellPlayer(String args) {
        infoBox.addText(args);
        infoBox.draw();
        mainInterface.refresh();
    }

    private void initEverything() {

        buildMap();
        buildDisplay();
        displayMap(); //show the map for the first time

    }

    private void buildMap() {//this will build all of the elements of the map

        int blockX, blockY, x, y; //these will be our random numbers when we need them

        //initiates the mapContents
        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < mapSizeY; k++) {
                mapContents[i][k] = new DebugObject();
            }
        }

        //Let's try to make random clouds
        for (int j = 0; j < (rng.nextInt(5) + 2); j++) {
            blockX = rng.nextInt(mapSizeX);
            blockY = rng.nextInt(mapSizeY);
            recurseNumber = 0;
            chaosMapping(new Point(blockX, blockY));
        }

        //Here is the initialization of the map with walls along the sides
        for (int i = 0; i < mapSizeX; i++) {
            mapContents[i][0] = new CloudObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30)));
            mapContents[i][mapSizeY - 1] = new CloudObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30)));
        }
        for (int i = 0; i < mapSizeY; i++) {
            mapContents[0][i] = new CloudObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30)));
            mapContents[mapSizeX - 1][i] = new CloudObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30)));
        }

        floorPlacement();
        mapContents[currentLoc.x][currentLoc.y] = new FloorObject();
        checkMapForConnectivity();
        creaturePlacement();

        //let's make sure the player is on an open space and not on a creature!
        mapContents[currentLoc.x][currentLoc.y] = new FloorObject();
        mapContents[currentLoc.x][currentLoc.y].visible = true;

        //let's make some stairs!
        boolean stairsPlaced = false;
        do {
            x = rng.nextInt(mapSizeX);//these lines give us a random point in the  map
            y = rng.nextInt(mapSizeY);
            if (mapContents[x][y] instanceof FloorObject) {
                mapContents[x][y] = new StairsObject();
                stairsPlaced = true;
            }
        } while (!stairsPlaced);
    }

    private void getPlayerName() {
        player.myName = mainInterface.askPlayer(1, "Please enter the particle's name. ");
        displayMap();
    }

    private void saveGame() {
        String fileName;
        fileName = player.myName + ".txt";
        BufferedWriter writer;

        try {
            writer = FileUtil.getWriter(fileName);

        } catch (IOException ioe) {
            System.out.println("Fatal Error Opening Writer for " + fileName);
            ioe.printStackTrace();
            return;
        }
        try {
            for (int i = 0; i < mapSizeX; i++) {
                for (int k = 0; k < mapSizeY; k++) {
                    writer.write(mapContents[i][k].objectOutput());
                }
            }
            writer.write(player.objectOutput());
            writer.write("currentLoc.x: " + eol + currentLoc.x + eol + eol);
            writer.write("currentLoc.y: " + eol + currentLoc.y + eol + eol);
            writer.write("mapLevel:" + eol + mapLevel + eol + eol);
            writer.write("xpLevels:" + eol);
            for (Integer xp : quarkLevels.levels) {
                writer.write(xp + eol);
            }
            writer.write(eol);
            writer.close();
        } catch (IOException ioe) {
            System.out.println("Fatal Error writing to " + fileName);
            ioe.printStackTrace();
            return;
        }
        tellPlayer("Game Saved!");
    }

    private void loadGame() {
        String fileName;
        getPlayerName();
        fileName = player.myName + ".txt";
        BufferedReader reader = null;
        String currentType;

        try {
            reader = FileUtil.getReader(fileName);

        } catch (IOException ioe) {
            mainInterface.askPlayer(2,"File does not exist. Press Enter to continue.");
            displayMap();
        }
        if (!(reader == null)) {
            try {
                for (int i = 0; i < mapSizeX; i++) {
                    for (int k = 0; k < mapSizeY; k++) {
//                        tellPlayer("currently: " + i + " " + k + " "); // for debugging
                        currentType = reader.readLine();
                        if (currentType.equals("GameObject")) {
                            mapContents[i][k] = new GameObject();
                        } else if (currentType.equals("CloudObject")) {
                            mapContents[i][k] = new CloudObject();
                        } else if (currentType.equals("DebugObject")) {
                            mapContents[i][k] = new DebugObject();
                        } else if (currentType.equals("FloorObject")) {
                            mapContents[i][k] = new FloorObject();
                        } else if (currentType.equals("MonsterObject")) {
                            mapContents[i][k] = new MonsterObject();
                        } else if (currentType.equals("NullObject")) {
                            mapContents[i][k] = new NullObject();
                        } else if (currentType.equals("StairsObject")) {
                            mapContents[i][k] = new StairsObject();
                        } else if (currentType.equals("WallObject")) {
                            mapContents[i][k] = new WallObject();
                        }


                        mapContents[i][k].pushObject(reader);
                        reader.readLine(); //gets rid of readability space between entries

                    }
                }
                reader.readLine(); //gets rid of entry title
                player.pushObject(reader);
                reader.readLine(); //gets rid of empty space between objects
                reader.readLine(); //gets rid of entry title
                currentLoc.x = Integer.valueOf(reader.readLine());
                reader.readLine(); //gets rid of empty space between objects
                reader.readLine(); //gets rid of entry title
                currentLoc.y = Integer.valueOf(reader.readLine());
                reader.readLine(); //gets rid of empty space between objects
                reader.readLine(); //gets rid of entry title
                mapLevel = Integer.valueOf(reader.readLine());
                reader.readLine(); //gets rid of empty space between objects
                reader.readLine(); //gets rid of entry title
                quarkLevels.pushObject(reader);
                reader.close();

                FileUtil.deleteFile(fileName);

                displayMap();
                mainInterface.refresh();
                tellPlayer("Game Loaded!");
            } catch (IOException ioe) {
                System.out.println("Fatal Error reading from " + fileName);
                ioe.printStackTrace();
                return;
            }
        }

    }
}