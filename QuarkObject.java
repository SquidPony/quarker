package my.quarker;

import java.util.Random;
import java.util.Vector;
import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author ehoward
 */
public class QuarkObject extends MonsterObject {

    private static final int RED = 0,  GREEN = 1,  BLUE = 2,  ANTIRED = 3,  ANTIGREEN = 4,  ANTIBLUE = 5;
    private int chromatic;
    private boolean anti;

    public QuarkObject(String myName, char represent, boolean passable, int hp,
            int damage, int aim, int agility, int level, int penetration,
            int toughness, int attackPower, int deflection, int satiation, int chrome) {
        initQuark(myName, represent, passable, hp, 0, damage, aim,
                agility, level, penetration, toughness,
                attackPower, deflection, satiation, chrome);
    }

    public QuarkObject(int level) {
        Random rng = new Random();
        int r;
        if (level < 3) {
            r = rng.nextInt(750) + 250;
        } else if (level < 5) {
            r = rng.nextInt(300) + 10;
        } else {
            r = rng.nextInt(75);
        }
        if (r < 15) {
            initQuark("truth quark", 't', false, 170900, 1800, 100, 50, 50, level,
                    50, 50, 50, 50, 10, -1);
        } else if (r < 40) {
            initQuark("beauty quark", 'b', false, 4250, 150, 50, 100, 50, level,
                    40, 40, 40, 40, 10, -1);
        } else if (r < 100) {
            initQuark("charm quark", 'c', false, 1250, 100, 50, 25, 20, level,
                    25, 25, 25, 25, 5, -1);
        } else if (r < 300) {
            initQuark("strange quark", 's', false, 105, 25, 25, 50, 20, level,
                    20, 20, 20, 20, 5, -1);
        } else if (r < 600) {
            initQuark("up quark", 'u', false, 3, 1, 2, 1, 2, level,
                    2, 2, 2, 2, 1, -1);
        } else {
            initQuark("down quark", 'd', false, 6, 2, 1, 2, 2, level,
                    3, 3, 3, 3, 1, -1);
        }

    }

    private void initQuark(String myName, char represent, boolean passable, int mass, 
            int plusminus, int damage, int aim, int agility, int level,
            int penetration, int toughness, int attackPower,
            int deflection, int satiation, int chrome) {

        Random rng = new Random();
        this.myName = myName;
        this.represent = represent;
        this.passable = passable;
        this.mass = mass - plusminus + rng.nextInt(plusminus * 2);
        this.damage = rng.nextInt(Math.max(1, mass / 20)) + (mass / 20) + damage;
        this.aim = aim;
        this.agility = agility;
        this.level = level;
        this.penetration = penetration;
        this.toughness = toughness;
        this.attackPower = attackPower;
        this.deflection = deflection;
        this.satiation = satiation;

        int tempChrome = chrome;
        if (tempChrome == -1) {
            tempChrome = rng.nextInt(6);
            if (tempChrome < 3) {
                anti = false;
            } else {
                anti = true;
            }
            if (anti) {
                this.setName("anti" + this.myName);
            }
        }


        this.chromatic = tempChrome;
        switch (tempChrome) {
            case RED:
                this.frontColor = CSIColor.RED_PIGMENT;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
            case GREEN:
                this.frontColor = CSIColor.GREEN;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
            case BLUE:
                this.frontColor = CSIColor.BLUE;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
            case ANTIRED:
                this.frontColor = CSIColor.CYAN;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
            case ANTIGREEN:
                this.frontColor = CSIColor.MAGENTA_DYE;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
            case ANTIBLUE:
                this.frontColor = CSIColor.YELLOW;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
        }
        this.feelings = new Vector<FeelingsObject>();

    }

    public void deepCopy(QuarkObject obj){
        deepCopy((MonsterObject)obj);
        chromatic = obj.chromatic;
        anti = obj.anti;
    }

    @Override
    public String getName() {
        String ret = "";

        switch (chromatic) {
            case RED:
                ret = "red ";
                break;
            case GREEN:
                ret = "green ";
                break;
            case BLUE:
                ret = "blue ";
                break;
            case ANTIRED:
                ret = "antired ";
                break;
            case ANTIGREEN:
                ret = "antigreen ";
                break;
            case ANTIBLUE:
                ret = "antiblue ";
                break;
        }
        return ret + this.myName;
    }
}
