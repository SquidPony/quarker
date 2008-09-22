package my.quarker;

import java.util.Random;
import java.util.Vector;
import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author ehoward
 */
public class QuarkObject extends MonsterObject{
    public static final QuarkObject TRUTH = new QuarkObject("truth quark", 't', false, 100, 100, 50, 50, 10),  BEAUTY = new QuarkObject("beauty quark", 'b', false, 100, 50, 100, 50, 10),  CHARM = new QuarkObject("charm quark", 'c', false, 50, 50, 25, 20, 5),  STRANGE = new QuarkObject("strange quark", 's', false, 50, 25, 50, 20, 5),  UP = new QuarkObject("up quark", 'u', false, 5, 2, 1, 2, 1),  DOWN = new QuarkObject("down quark", 'd', false, 5, 1, 2, 2, 1),  
        ANTIUP = new QuarkObject("antiup quark", 'u', false, 5, 2, 1, 2, 1);
    private static final int RED = 0,  GREEN = 1,  BLUE = 2,  ANTIRED = 3,  ANTIGREEN = 4,  ANTIBLUE = 5;

    private int chromatic;
    private boolean anti;
    
    public QuarkObject(String myName, char represent, boolean passable, int hp, int damage, int attack, int defense, int satiation){
        this(myName, represent, passable, hp, damage, attack, defense, satiation, -1);
    }
    
        public QuarkObject(QuarkObject mon) {
            this(mon.myName, mon.represent, mon.passable, mon.hp, mon.damage, mon.attack, mon.defense, mon.satiation, -1);
    }
    
        public QuarkObject(String myName, char represent, boolean passable, int hp, int damage, int attack, int defense, int satiation, int chrome) {
        this.myName = myName;
        this.represent = represent;
        this.passable = passable;
        this.hp = hp;
        this.damage = damage;
        this.attack = attack;
        this.defense = defense;
        this.satiation = satiation;
        if (chrome == -1){
            Random rng = new Random();
            chrome = rng.nextInt(6);
        }
        
        
        this.chromatic = chrome;
        switch (chrome){
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
                this.frontColor = CSIColor.BLUE;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
            case ANTIGREEN:
                this.frontColor = CSIColor.BLUE;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
            case ANTIBLUE:
                this.frontColor = CSIColor.BLUE;
                this.backColor = BaseObject.DEFAULT_BACK_COLOR;
                break;
        }
        this.feelings = new Vector<FeelingsObject>();

    }
}
