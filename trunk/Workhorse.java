package my.quarker;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import net.slashie.libjcsi.*;
import net.slashie.libjcsi.textcomponents.*;
import net.slashie.libjcsi.wswing.*;
import net.slashie.util.FileUtil;

public class Workhorse {
    //-------------------Custom variables-------------------//
    private String versionNumber = "0.05";
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
    private MapObject[][] mapContents = new MapObject[mapSizeX][mapSizeY];
    private Point currentLoc = new Point((mapSizeX / 2), (mapSizeY / 2));
    private XpLevels quarkLevels = new XpLevels();
    private PlayerObject player = new PlayerObject();
    private ArrayList<CSIColor> colorList = new ArrayList<CSIColor>();
    private String eol = System.getProperty("line.separator");

    public Workhorse() {
        try {
            mainInterface = new WSwingConsoleInterface("Quarker", true);
        } catch (ExceptionInInitializerError eiie) {
            System.out.println("Fatal Error Initializing Swing Console Box");
            eiie.printStackTrace();
            System.exit(-1);
        }
        initEverything();
        mainInterface.refresh();
        getPlayerName();
        playerTurn();
    }

    public String askPlayer(int lines, String question, CSIColor color) {
        int x, y;
        String answer;
        mainInterface.saveBuffer();

        DialogBox dialog = new DialogBox(mainInterface, lines, question);
        dialog.setForeColor(color);
        x = (mainInterface.xdim / 2) - (dialog.getWidth() / 2);
        y = (mainInterface.ydim / 2) - (dialog.getHeight() / 2);
        dialog.setPosition(x, y);

        dialog.setText(question);
        mainInterface.locateCaret(x + 2, y + lines + 2);
        dialog.draw();
        mainInterface.refresh();

        answer = mainInterface.input();
        mainInterface.restore();
        mainInterface.refresh();
        return answer;
    }

    public String askPlayer(int lines, String question) {
        return askPlayer(lines, question, CSIColor.WHITE);
    }

    private void playerTurn() {// this will get the player's input and launch the method that checks what was input
        CharKey actionKey = new CharKey();

        do {
            actionKey = mainInterface.inkey();
            infoBox.clear();
            takeAction(actionKey);
        } while (true);
    }

    private void runTurn() { // here is where everything happens after the player does something which takes time
        runMonsterTurn();
        checkStats(); // this will see if the player has leveled up or maybe died.
        displayMap(); // this should most likey be the last thing that happens before the player gets to go again
        mainInterface.refresh();
    }
    
    private void restTurn(){
        runTurn();
        //eventualy there should be healing here as well
    }

    private void takeAction(CharKey thisKey) {  // this will feed control to the appropriate method based on player input
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
            } else if (thisKey.isSelfArrow()){
                restTurn();
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
                if ((mapContents[i][k].hasMonster())) {
                    MonsterObject monObj = mapContents[i][k].getMonster();
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
                            MapObject tempObj = mapContents[i + x][k + y];
                            if (tempObj.getTopObject() instanceof TerrainObject) {
                                mapContents[i][k].setMonster(null);
                                tempObj.setMonster(monObj);
                            } else {
                                tellPlayer("The " + monObj.myName + " glares at the " + tempObj.getTopObjectName() + " that's in its way.");
                            }
                        }
                    }
                }
            }
        }
    }

    private void monsterAttacks(int a, int b) {
        MonsterObject monster = mapContents[a][b].getMonster();
        int attack;
        attack = monster.getAttack();
        if ((attack + rng.nextInt(10)) > (player.level * 3 + rng.nextInt(30))) {
            player.hp -= monster.getDamage();
            tellPlayer("The " + monster.myName + " reduced your mass by " + String.valueOf(monster.getDamage()) + " electrovolts!");
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
        leaving("");
    }

    private void leaving(String args) {
        askPlayer(2, args + "Thanks for playing.  Press Enter to exit now.");
        System.exit(0);
    }

    private void checkStats() {
        if (player.hp < 1) {
            tellPlayer("You have become massless and unable to effect your surroundings.");
            leaving();

        }
        if (player.size >= player.nextSize) {
            levelUp();
        }
    }

    private void tryToGoDownStairs() {
        if (mapContents[currentLoc.x][currentLoc.y].isStairs()) {
            StairsObject nowObj = (StairsObject) mapContents[currentLoc.x][currentLoc.y].getFlooring();
            if (nowObj.down) {
                buildNewLevel(true);
            } else {
                tellPlayer("This wormhole leads the other direction in spacetime.");
            }
        } else {
            tellPlayer("You don't see a wormhole here.");
        }
    }

    private void tryToGoUpStairs() {
        if (mapContents[currentLoc.x][currentLoc.y].isStairs()) {
            StairsObject nowObj = (StairsObject) mapContents[currentLoc.x][currentLoc.y].getFlooring();
            if (!(nowObj.down)) {
                buildNewLevel(false);
            } else {
                tellPlayer("This wormhole leads the other direction in spacetime.");
            }
        } else {
            tellPlayer("You don't see a wormhole here.");
        }
    }

    private void tryToMove(int a, int b) {
        MapObject newObj = mapContents[a][b];

        if (newObj.isPassable()) {
            currentLoc.move(a, b);
        } else {
            if (newObj.hasMonster()) {
                if (doFight(newObj.getMonster())) {
                    newObj.setMonster(null);
                }
            } else {
                tellPlayer("You can't move through the " + newObj.getTopObjectName() + "!");
            }
        }
        runTurn();
    }

    public boolean doFight(MonsterObject monster) {


        int r, i;

        r = rng.nextInt(100);
        r = r + player.level * 5 - monster.getDefense();

        monster.wakeUp(); //attacking will cause the monster to wake up, even if not hit

        if (r < 50) {
            //miss
            tellPlayer("You missed the " + monster.myName);
            return false;
        } else {
            i = rng.nextInt(player.level * 4);
            if (i <= 0) {
                tellPlayer("You hit the " + monster.myName + ", but didn't effect it!");
            } else {
                tellPlayer("You hit the " + monster.myName + " causing it to lose " + i + " electronvolts of mass.");
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

        String beol = " XXX ";//end of line for TextBox
        BaseObject nowContents;
        for (int k = 0; k < mapSizeX; k++) {
            for (int i = 0; i < mapSizeY; i++) {
                nowContents = mapContents[k][i].getTopObject();
                mainInterface.print(k, i + 2, nowContents.represent, nowContents.myColor);
            }
        }
        mainInterface.print(currentLoc.x, currentLoc.y + infoSpace, player.represent, player.myColor);
        infoBox.draw();
        statsBox.setText(
            beol + "Mass: " + beol + player.hp + beol + beol + "Size: " + beol + player.level + beol + beol + "Xp: " + beol + player.size + beol + beol + "Spacetime: " + beol + mapLevel + beol);
        statsBox.draw();
        mainInterface.refresh();
    }

    private CSIColor checkColorList(CSIColor color) {
        CSIColor tempColor;
        if (!colorList.contains(color)) {
            colorList.add(color);
        }
        tempColor = colorList.get(colorList.indexOf(color));

        return tempColor;
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
        CSIColor tempColor;
        if (recurseNumber > maxRecurse) {
            return;
        }

        int i = start.x;
        int k = start.y;
        if (!((i > (mapSizeX - 1)) || (k > (mapSizeY - 1)) || (i < 0) || (k < 0))) { //make sure we're not over the edge of the map
            tempColor = checkColorList(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30)));
            mapContents[i][k].setFlooring(new FoldObject(tempColor));
        }
        recurseNumber++;
        i = i + 1 - rng.nextInt(3);
        k = k + 1 - rng.nextInt(3);
        chaosMapping(new Point(i, k));
    }

    private void creaturePlacement() {// this will place creatures randomly
        int r; //this will hold our random numbers
        MapObject tempObj;
        //let's fill in the rest randomly for now
        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < (mapSizeY); k++) {
                tempObj = mapContents[i][k];
                if (!(tempObj.isWall())) {
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
                            tempObj.setMonster(new MonsterObject(MonsterTypeEnum.TRUTH));
                        } else if (r < 40) {
                            tempObj.setMonster(new MonsterObject(MonsterTypeEnum.BEAUTY));
                        } else if (r < 100) {
                            tempObj.setMonster(new MonsterObject(MonsterTypeEnum.CHARM));
                        } else if (r < 300) {
                            tempObj.setMonster(new MonsterObject(MonsterTypeEnum.STRANGE));
                        } else if (r < 600) {
                            tempObj.setMonster(new MonsterObject(MonsterTypeEnum.UP));
                        } else {
                            tempObj.setMonster(new MonsterObject(MonsterTypeEnum.DOWN));
                        }
                    } //else {mapContents[i][k] = new FloorObject();}
                }
            }
        }
    }

    private void floorPlacement() {

        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < (mapSizeY); k++) {
                if (!(mapContents[i][k].isWall())) {
                    mapContents[i][k].setFlooring();
                }
            }
        }
    }

    private void floodFill(int x, int y) {
        if ((x < 0) || (x > (mapSizeX - 1)) || (y < 0) || (y > (mapSizeY - 1))) {
            return; //exits if coords are out of range for the map
        }
        if (mapContents[x][y].isVisible()) {
            return;
        }
        if (!(mapContents[x][y].isWall())) {
            mapContents[x][y].getTopObject().visible = true;
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
            if (mapContents[x][y].isWall()) {
                mapContents[x][y].setFlooring();
            }
        } while (!(mapContents[x][y].isVisible()));

    }

    private void checkMapForConnectivity() {

        floodFill(currentLoc.x, currentLoc.y);

        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < (mapSizeY); k++) {
                if (!(mapContents[i][k].isWall()) && !(mapContents[i][k].isVisible())) {
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
            mapContents[currentLoc.x][currentLoc.y].setFlooring(new StairsObject(false));
            tellPlayer("You have travelled through a wormhole and found a new area of spacetime to conquer.");
        } else {
            mapLevel--;
            buildMap();
            tellPlayer("You travel backwards through the wormhole to the last spacetime area you were in. The wormhole collapses behind you.");
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
                mapContents[i][k] = new MapObject();
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
            mapContents[i][0].setFlooring(new FoldObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30))));
            mapContents[i][mapSizeY - 1].setFlooring(new FoldObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30))));
        }
        for (int i = 0; i < mapSizeY; i++) {
            mapContents[0][i].setFlooring(new FoldObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30))));
            mapContents[mapSizeX - 1][i].setFlooring(new FoldObject(new CSIColor((rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30), (rng.nextInt(100) + rng.nextInt(100) + 30))));
        }

        floorPlacement();
        mapContents[currentLoc.x][currentLoc.y].setFlooring(TerrainObject.DEFAULT);
        checkMapForConnectivity();
        creaturePlacement();

        //let's make sure the player is on an open space and not on a creature!
        mapContents[currentLoc.x][currentLoc.y].setMonster(null);
        mapContents[currentLoc.x][currentLoc.y].getTopObject().visible = true;

        //let's make some stairs!
        boolean stairsPlaced = false;
        do {
            x = rng.nextInt(mapSizeX);//these lines give us a random point in the  map
            y = rng.nextInt(mapSizeY);
            if (mapContents[x][y].isFloor()) {
                mapContents[x][y].setFlooring(new StairsObject());
                stairsPlaced = true;
            }
        } while (!stairsPlaced);
    }

    private void getPlayerName() {
        player.myName = askPlayer(1, "Please enter the particle's name. ");
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
                    mapContents[i][k].objectOutput(writer);
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
        tellPlayer("Spacetime Continuum Saved!");
    }

    private void loadGame() {
        String fileName;
        getPlayerName();
        fileName = player.myName + ".txt";
        BufferedReader reader = null;
        String currentType;
        MapObject oldObj = new MapObject();

        try {
            reader = FileUtil.getReader(fileName);

        } catch (IOException ioe) {
            askPlayer(2, "File does not exist. Press Enter to continue.");
            displayMap();
        }
        if (reader != null) {
            try {
                currentType = reader.readLine();
                for (int i = 0; i < mapSizeX; i++) {
                    for (int k = 0; k < mapSizeY; k++) {
//                        tellPlayer("currently: " + i + " " + k + ", last: " + oldObj.getTopObjectName() + " "); // for debugging
                        mapContents[i][k].pushObject(reader, currentType);
//                        oldObj = mapContents[i][k]; // for debugging
//                        displayMap(); // for debugging
//                        mainInterface.refresh(); // for debugging

                    }
                }
                
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
                tellPlayer("Spacetime Continuum Restored.");
            } catch (IOException ioe) {
                System.out.println("Fatal Error reading from " + fileName);
                ioe.printStackTrace();
                return;
            }
        }

    }
}