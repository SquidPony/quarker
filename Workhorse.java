package my.quarker;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import net.slashie.libjcsi.*;
import net.slashie.libjcsi.textcomponents.*;
import net.slashie.libjcsi.wswing.*;

public class Workhorse {
    //-------------------Custom variables-------------------//

    private String versionNumber = "0.06";
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
    static final private int[][] FOV_MULTIPLIER = {
        {1, 0, 0, -1, -1, 0, 0, 1},
        {0, 1, -1, 0, 0, -1, 1, 0},
        {0, 1, 1, 0, 0, -1, -1, 0},
        {1, 0, 0, 1, -1, 0, 0, -1},};

    public Workhorse() {
        try {
            mainInterface = new WSwingConsoleInterface("Quarker");
        } catch (ExceptionInInitializerError eiie) {
            System.out.println("Fatal Error Initializing Swing Console Box");
            eiie.printStackTrace();
            System.exit(-1);
        }
        initEverything();
        mainInterface.refresh();
        initializePlayer();
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

    public void notifyPlayer(int lines, String notification, CSIColor color) {
        int x, y;
        mainInterface.saveBuffer();

        DialogBox dialog = new DialogBox(mainInterface, lines, notification);
        dialog.setForeColor(color);
        x = (mainInterface.xdim / 2) - (dialog.getWidth() / 2);
        y = (mainInterface.ydim / 2) - (dialog.getHeight() / 2);
        dialog.setPosition(x, y);

        dialog.setText(notification);
        mainInterface.locateCaret(x + 2, y + lines + 2);
        dialog.draw();
        mainInterface.refresh();

        mainInterface.inkey();
        mainInterface.restore();
        mainInterface.refresh();
    }

    public void notifyPlayer(int lines, String notification) {
        notifyPlayer(lines, notification, CSIColor.WHITE);
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

    private void restTurn() {
        runTurn();
    //eventualy there should be healing here as well
    }

    private void takeAction(CharKey thisKey) {  // this will feed control to the appropriate method based on player input

//        tellPlayer(thisKey.toString());//for debugging purposes

        int x = currentLoc.x,
                y = currentLoc.y;

        if (thisKey.isUpArrow() || (thisKey.code == CharKey.k)) {
            tryToMove(x, y - 1);
        } else if (thisKey.isUpRightArrow() || (thisKey.code == CharKey.u)) {
            tryToMove(x + 1, y - 1);
        } else if (thisKey.isRightArrow() || (thisKey.code == CharKey.l)) {
            tryToMove(x + 1, y);
        } else if (thisKey.isDownRightArrow() || (thisKey.code == CharKey.n)) {
            tryToMove(x + 1, y + 1);
        } else if (thisKey.isDownArrow() || (thisKey.code == CharKey.j)) {
            tryToMove(x, y + 1);
        } else if (thisKey.isDownLeftArrow() || (thisKey.code == CharKey.b)) {
            tryToMove(x - 1, y + 1);
        } else if (thisKey.isLeftArrow() || (thisKey.code == CharKey.h)) {
            tryToMove(x - 1, y);
        } else if (thisKey.isUpLeftArrow() || (thisKey.code == CharKey.y)) {
            tryToMove(x - 1, y - 1);
        } else if (thisKey.isSelfArrow() || (thisKey.code == CharKey.DOT)) {
            restTurn();
        }

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
            case CharKey.L:
                lookAround();
                break;
            case CharKey.B:
                buildNewLevel(true);
                break;
            case CharKey.QUESTION:
                showHelp();
                break;
        }
    }

    private void showHelp() {
    }

    private void runMonsterTurn() {
        int x, y;
        for (int i = 0; i < mapSizeX; i++) {
            for (int k = 0; k < mapSizeY; k++) {
                if ((mapContents[i][k].hasMonster())) {
                    MonsterObject monObj = mapContents[i][k].getMonster();
                    if (monObj.isAwake()) {
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
                                tellPlayer("The " + monObj.getName() + " glares at the " + tempObj.getTopObjectName() + " that's in its way.");
                            }
                        }
                    }
                }
            }
        }
    }

    private void monsterAttacks(int a, int b) {
        MonsterObject monster = mapContents[a][b].getMonster();
        double hitChance;

        /* check to see if monster hits*/
        hitChance = monster.getAim() + monster.getLevel() - player.getAgility();
        hitChance *= 150;
        hitChance /= monster.getAim();
        hitChance = Math.max(1, hitChance); // there is always a 1% chance for the monster to hit
        if (hitChance > rng.nextInt(100)) {// a hit!
            int currentDamage = monster.getDamage();
            if (monster.getPenetration() > player.getDeflection()) {
                currentDamage += monster.getPenetration() - player.getDeflection();
            }
            currentDamage -= player.getToughness();
            if (currentDamage < 0) {
                currentDamage = 0;
            }
            player.applyDamage(currentDamage);
            tellPlayer("The " + monster.getName() + " reduced your mass by " + String.valueOf(currentDamage) + " electrovolts!");
        } else {
            tellPlayer("The " + monster.getName() + " missed you.");
        }
    }

    private void levelUp() {
        player.setNextSize((int) quarkLevels.levels.removeFirst());
        player.levelUp();
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
        if (player.getMass() < 1) {
            tellPlayer("You have become massless and unable to effect your surroundings.");
            leaving();

        }
        if (player.getSize() >= player.getNextSize()) {
            levelUp();
        }
    }

    private void lookAround() {
        CharKey actionKey = new CharKey();
        BaseObject cursor = new BaseObject("cursor", '?', true);
        Point cursorLoc = new Point(currentLoc.x, currentLoc.y);

        do {
            actionKey = mainInterface.inkey();
            infoBox.clear();
            cursorLoc = doLooking(actionKey, cursorLoc);
            displayMap();
            mainInterface.print(cursorLoc.x, cursorLoc.y + infoSpace, cursor.represent, CSIColor.AMBER);
        } while (true);
    }

    private Point doLooking(CharKey thisKey, Point current) {
        Point tempLoc = current;
        if (thisKey.isArrow()) {
            if (thisKey.isUpArrow()) {
                tempLoc = new Point(current.x, current.y - 1);
            } else if (thisKey.isUpRightArrow()) {
                tempLoc = new Point(current.x + 1, current.y - 1);
            } else if (thisKey.isRightArrow()) {
                tempLoc = new Point(current.x + 1, current.y);
            } else if (thisKey.isDownRightArrow()) {
                tempLoc = new Point(current.x + 1, current.y + 1);
            } else if (thisKey.isDownArrow()) {
                tempLoc = new Point(current.x, current.y + 1);
            } else if (thisKey.isDownLeftArrow()) {
                tempLoc = new Point(current.x - 1, current.y + 1);
            } else if (thisKey.isLeftArrow()) {
                tempLoc = new Point(current.x - 1, current.y);
            } else if (thisKey.isUpLeftArrow()) {
                tempLoc = new Point(current.x - 1, current.y - 1);
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
                case CharKey.ENTER:
                    tellPlayer(mapContents[tempLoc.x][tempLoc.y].getTopObjectName());
            }
        }
        return tempLoc;
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
//            checkVisibility();
//            doFov(currentLoc.x, currentLoc.y, player.getViewRange());
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

        monster.wakeUp(); //attacking will cause the monster to wake up, even if not hit
    /* check to see if player hits */
        double hitChance;
        hitChance = player.getAim() + player.getLevel() - monster.getAgility();
        hitChance *= 150;
        hitChance /= player.getAim();
        hitChance = Math.max(5, hitChance); // there is always a 5% chance for the player to hit
        if (hitChance > rng.nextInt(100)) {// a hit!
            int currentDamage = player.getDamage();
            if (player.getPenetration() > monster.getDeflection()) {
                currentDamage += player.getPenetration() - monster.getDeflection();
            }
            currentDamage -= monster.getToughness();
            if (currentDamage < 1) {
                currentDamage = 1;
            }
            tellPlayer("You hit the " + monster.getName() + " causing it to lose " + currentDamage + " electronvolts of mass.");
            monster.applyDamage(currentDamage);
            if (monster.getMass() < 0) {//swallow!
                tellPlayer("You absorbed the " + monster.getName() + "!");
                player.setSize(player.getSize() + monster.getSatiation());
                return true;
            }
         else {
            return false;
         }
        }
        tellPlayer("You missed the " + monster.getName() + ".");
        return false;
    }

    private void doFov(int startX, int startY, int radius) {

        mapContents[startX][startY].setVisible(100);
        for (int i = 0; i < 8; i++) {
            castLight(startX, startY, 1, 1.0, 0.0, radius,
                    FOV_MULTIPLIER[0][i], FOV_MULTIPLIER[1][i],
                    FOV_MULTIPLIER[2][i], FOV_MULTIPLIER[3][i], 0);
        }
//        castLight(startX, startY, 1, 1.0, 0.0, radius,
//            1, 0, 0, 1, 0); //testing
    }

    private void castLight(int cx, int cy, int row, double lightStart, double lightEnd, int radius, int xx, int xy, int yx, int yy, int id) {
        if (lightStart < lightEnd) {
            return;
        }
        double radius2 = Math.pow(radius, 2), lSlope, rSlope, newStart = 0;
        int dx, dy, mx, my;
        boolean blocked;
        for (int i = row; i <= radius; i++) {
            dx = -i - 1;
            dy = -i;
            blocked = false;
            while (dx <= 0) {
                dx += 1;
//                # Translate the dx, dy co-ordinates into map co-ordinates
                mx = cx + dx * xx + dy * xy;
                my = cy + dx * yx + dy * yy;
//                # l_slope and r_slope store the slopes of the left and right
//                # extremities of the square we're considering:
                lSlope = (dx - 0.5) / (dy + 0.5);
                rSlope = (dx + 0.5) / (dy - 0.5);
                if (lightStart < rSlope) {
                    continue;
                } else if (lightEnd > lSlope) {
                    break;
                } else {
//                    # Our light beam is touching this square; light it
                    if ((dx * dx + dy * dy) < radius2) {
                        mapContents[mx][my].setVisible(100);
                        mapContents[mx][my].hasBeenSeen();
                        mapContents[mx][my].setChanged();
                    }
                    if (blocked) {
//                        # We've scanning a row of blocked squares
                        if (mapContents[mx][my].isWall()) {
                            newStart = rSlope;
                            continue;
                        } else {
                            blocked = false;
                            lightStart = newStart;
                        }
                    } else {
                        if (mapContents[mx][my].isWall() && (i < radius)) {
//                            # This is a blocking square, start a child scan
                            blocked = true;
                            castLight(cx, cy, i + 1, lightStart, lSlope,
                                    radius, xx, xy, yx, yy, id + 1);
                            newStart = rSlope;
                        }
                    }
                }
            } //# while dx <= 0

            if (blocked) {
                break;
            }
        }
    }

    private boolean isLOSVisible(int startX, int startY, int lookX, int lookY) {
        if ((startX == lookX) && (startY == lookY)) {
            return true;
        }
        if (mapContents[startX][startY].isWall()) {
            return false;
        }
        int moveX = 0, moveY = 0;
        if (lookX < startX) {
            moveX = -1;
        }
        if (lookX > startX) {
            moveX = 1;
        }
        if (lookY < startY) {
            moveY = -1;
        }
        if (lookY > startY) {
            moveY = 1;
        }
        return isLOSVisible(startX + moveX, startY + moveY, lookX, lookY);
    }

    private void checkVisibility() {
        int r = player.getViewRange();

        int x = currentLoc.x, y = currentLoc.y;
        MapObject map;
        int minx = Math.max(0, (x - r - 2));
        int maxx = Math.min(mapSizeX, x + r + 2);
        int miny = Math.max(0, (y - r - 2));
        int maxy = Math.min(mapSizeY, y + r + 2);
        ArrayList<Integer> slopes = new ArrayList<Integer>();

        for (int k = minx; k < maxx; k++) {
            for (int i = miny; i < maxy; i++) {
                map = mapContents[k][i];
                if (Math.pow(k - x, 2) + Math.pow(i - y, 2) <= Math.pow(r, 2)) {
                    if (isLOSVisible(x, y, k, i)) {
                        map.setVisible(100);
                        map.setHasBeenSeen();
                    } else {
                        map.setVisible(0);
                    }
                } else {
                    map.setVisible(0);
                }
                map.setChanged();
            }
        }
    }

    private void initVisibility() {
        int r = player.getViewRange();
        MapObject map;

        int x = currentLoc.x, y = currentLoc.y;
        for (int k = 0; k < x; k++) {
            for (int i = 0; i < y; i++) {
                map = mapContents[k][i];
//                if (Math.pow(k - x, 2) + Math.pow(i - y, 2) <= Math.pow(r, 2)) {
//                    map.setVisible(100);
//                } else {
                map.setVisible(0);
//                }
                map.setChanged();
                map.setHasBeenSeen(false);
            }
        }
    }

    private void cleanDisplay() {
        String beol = " XXX ";//end of line for TextBox

        BaseObject nowContents;
        MapObject map;
        for (int k = 0; k < mapSizeX; k++) {
            for (int i = 0; i < mapSizeY; i++) {
                map = mapContents[k][i];
                if (map.hasBeenSeen()) {
                    if (map.isVisible()) {
                        nowContents = map.getTopObject();
                        mainInterface.print(k, i + infoSpace, nowContents.represent, nowContents.frontColor, nowContents.backColor);
                    } else {
                        nowContents = map.getFlooring();
                        mainInterface.print(k, i + infoSpace, nowContents.represent, faded(nowContents.frontColor), nowContents.backColor);
                    }
                } else {
                    mainInterface.print(k, i, ' ', CSIColor.BLACK);
                }

                map.setChanged(false);
            }
        }
    }

    private void displayMap() {

        String beol = " XXX ";//end of line for TextBox

        BaseObject nowContents;
        MapObject map;

        for (int k = 0; k < mapSizeX; k++) {
            for (int i = 0; i < mapSizeY; i++) {
                map = mapContents[k][i];
//                if (map.hasBeenSeen()) {
//                    if (map.isChanged()) {
//                        if (map.isVisible()) {
                nowContents = map.getTopObject();
                mainInterface.print(k, i + infoSpace, nowContents.represent, nowContents.frontColor, nowContents.backColor);
//                        } else {
//                            nowContents = map.getFlooring();
//                            mainInterface.print(k, i + infoSpace, nowContents.represent, faded(nowContents.frontColor), nowContents.backColor);
//                        }
//                    }
                map.setChanged(false);
//                } else {
//                    mainInterface.print(k, i, ' ', CSIColor.BLACK);
//                }
            }
        }
        mainInterface.print(currentLoc.x, currentLoc.y + infoSpace, player.represent, player.frontColor);
        infoBox.draw();
        statsBox.setText(
                beol + "Mass: " + beol + player.getMass() + beol + beol + "Level: " + beol + player.getLevel() + beol + beol + "Xp: " + beol + player.getSize() + beol + beol + "Spacetime: " + beol + mapLevel + beol);
        statsBox.draw();
        mainInterface.refresh();
//        displayLightMap();
    }

    private void displayLightMap() {
        String beol = " XXX ";//end of line for TextBox

        BaseObject nowContents;
        MapObject map;

        for (int k = 0; k < mapSizeX; k++) {
            for (int i = 0; i < mapSizeY; i++) {
                map = mapContents[k][i];
                if (map.isVisible()) {
                    mainInterface.print(k, i + infoSpace, '!', CSIColor.ALICE_BLUE, CSIColor.YELLOW);
                } else {

                    mainInterface.print(k, i + infoSpace, 'X', CSIColor.SAFETY_ORANGE, CSIColor.VERMILION);
                }
            }
        }
        mainInterface.print(currentLoc.x, currentLoc.y + infoSpace, player.represent, player.frontColor);
        infoBox.draw();
        statsBox.setText(
                beol + "Mass: " + beol + player.getMass() + beol + beol + "Size: " + beol + player.getLevel() + beol + beol + "Xp: " + beol + player.getSize() + beol + beol + "Spacetime: " + beol + mapLevel + beol);
        statsBox.draw();
        mainInterface.refresh();
    }

    private CSIColor faded(CSIColor color) {
        return checkColorList(new CSIColor(Math.max(0, (color.getR() - 80)), Math.max(0, color.getG() - 80), Math.max(0, color.getB() - 80)));
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
                if (tempObj.isFloor()) {
                    r = rng.nextInt(25); //about 1/25th of the tiles will have an enemy

                    if (r == 0) {
                        tempObj.setMonster(new QuarkObject(mapLevel));
                    }
                }
            }
        }
    }

    private void floorPlacement(boolean different) {

        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < (mapSizeY); k++) {
                if (!(mapContents[i][k].isWall())) {
                    if (different) {
                        mapContents[i][k].setFlooring(new FloorObject());
                    } else {
                        mapContents[i][k].setFlooring();
                    }
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
            mapContents[x][y].setVisible(100);
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
                mapContents[x][y].setFlooring(new FloorObject());
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
        initVisibility();
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
        initVisibility();
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

        floorPlacement(true);
        mapContents[currentLoc.x][currentLoc.y].setFlooring(new FloorObject());
        checkMapForConnectivity();
//        floorPlacement(false);
        creaturePlacement();

        //let's make sure the player is on an open space and not on a creature!
        mapContents[currentLoc.x][currentLoc.y].setMonster(null);

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

        for (int i = 0; i < (mapSizeX); i++) {
            for (int k = 0; k < mapSizeY; k++) {
                mapContents[i][k].setVisible(false);
            }
        }
    }

    private void initializePlayer() {
        player.myName = askPlayer(1, "Please enter the particle's name. ");
        player.initialize();
        cleanDisplay();
//        checkVisibility();
//        doFov(currentLoc.x, currentLoc.y, player.getViewRange());
        displayMap();
    }

    private void saveGame() {
//        String fileName;
//        fileName = player.myName + ".txt";
//        BufferedWriter writer;
//
//        try {
//            writer = FileUtil.getWriter(fileName);
//
//        } catch (IOException ioe) {
//            System.out.println("Fatal Error Opening Writer for " + fileName);
//            ioe.printStackTrace();
//            return;
//        }
//        try {
//            for (int i = 0; i < mapSizeX; i++) {
//                for (int k = 0; k < mapSizeY; k++) {
//                    mapContents[i][k].objectOutput(writer);
//                }
//            }
//            writer.write(player.outputObjectToFile());
//            writer.write("currentLoc.x: " + eol + currentLoc.x + eol + eol);
//            writer.write("currentLoc.y: " + eol + currentLoc.y + eol + eol);
//            writer.write("mapLevel:" + eol + mapLevel + eol + eol);
//            writer.write("xpLevels:" + eol);
//            for (Integer xp : quarkLevels.levels) {
//                writer.write(xp + eol);
//            }
//            writer.write(eol);
//            writer.close();
//        } catch (IOException ioe) {
//            System.out.println("Fatal Error writing to " + fileName);
//            ioe.printStackTrace();
//            return;
//        }
//        tellPlayer("Spacetime Continuum Saved!");
        tellPlayer("Saving is currently disabled.");
    }

    private void loadGame() {
//        String fileName;
//        getPlayerName();
//        fileName = player.myName + ".txt";
//        BufferedReader reader = null;
//        String currentType;
//        MapObject oldObj = new MapObject();
//
//        try {
//            reader = FileUtil.getReader(fileName);
//
//        } catch (IOException ioe) {
//            askPlayer(2, "File does not exist. Press Enter to continue.");
//            displayMap();
//        }
//        if (reader != null) {
//            try {
//                currentType = reader.readLine();
//                for (int i = 0; i < mapSizeX; i++) {
//                    for (int k = 0; k < mapSizeY; k++) {
////                        tellPlayer("currently: " + i + " " + k + ", last: " + oldObj.getTopObjectName() + " "); // for debugging
//                        mapContents[i][k].pushObject(reader, currentType);
////                        oldObj = mapContents[i][k]; // for debugging
////                        displayMap(); // for debugging
////                        mainInterface.refresh(); // for debugging
//
//                    }
//                }
//
//                player.inputObjectFromFile(reader);
//                reader.readLine(); //gets rid of empty space between objects
//                reader.readLine(); //gets rid of entry title
//                currentLoc.x = Integer.valueOf(reader.readLine());
//                reader.readLine(); //gets rid of empty space between objects
//                reader.readLine(); //gets rid of entry title
//                currentLoc.y = Integer.valueOf(reader.readLine());
//                reader.readLine(); //gets rid of empty space between objects
//                reader.readLine(); //gets rid of entry title
//                mapLevel = Integer.valueOf(reader.readLine());
//                reader.readLine(); //gets rid of empty space between objects
//                reader.readLine(); //gets rid of entry title
//                quarkLevels.pushObject(reader);
//                reader.close();
//
//                FileUtil.deleteFile(fileName);
//
//                displayMap();
//                mainInterface.refresh();
//                tellPlayer("Spacetime Continuum Restored.");
//            } catch (IOException ioe) {
//                System.out.println("Fatal Error reading from " + fileName + " " + ioe.getMessage());
//                ioe.printStackTrace();
//                return;
//            }
//        }
        tellPlayer("Loading currently disabled.");
    }
}
