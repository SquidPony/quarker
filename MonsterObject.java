/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

import java.io.*;
import java.util.Vector;
import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author Eben
 */
public class MonsterObject extends BaseObject {

    private int hp, //current health of the monster
         damage, //amount of base damage the monster will do
         attack, //base chance of hit on attack
         defense, //base defense against attack
         satiation, //how full player will get from absorbing the monster
         x,  y; //current x,y coordinates for the monster
    private Vector<FeelingsObject> feelings = null;
    private boolean awake = false; //will tell us if the monster is active, default is not active
    
    /* The following constants are to make default monsters
     * of the listed types
     */
      public static final MonsterObject TRUTH = new MonsterObject("truth quark",'t',false,CSIColor.MAGENTA,100,100,50,50,10), 
    BEAUTY = new MonsterObject("beauty quark",'b',false,CSIColor.GREEN,100,50,100,50,10),
    CHARM = new MonsterObject("charm quark",'c',false,CSIColor.GRAY,50,50,25,20,5),
    STRANGE = new MonsterObject("strange quark",'s',false,CSIColor.CYAN,50,25,50,20,5),
    UP = new MonsterObject("up quark",'u',false,CSIColor.YELLOW,5,2,1,2,1),
    DOWN = new MonsterObject("down quark",'d',false,CSIColor.BLUE,5,1,2,2,1),
    ANTIUP = new MonsterObject("antiup quark", 'u',false,CSIColor.YELLOW,5,2,1,2,1);

    MonsterObject() {
        super();
    }
    
    
    public MonsterObject(MonsterObject mon) {
        myName = mon.myName;
        represent = mon.represent;
        passable = mon.passable;
        myColor = mon.myColor;
        hp = mon.hp;
        damage = mon.damage;
        attack = mon.attack;
        defense = mon.defense;
        satiation = mon.satiation;
    }
    
    //this constructor lets us set everything, probably useful for unique monsters
    public MonsterObject(String myName, char represent, boolean passable, CSIColor myColor, int hp, int damage, int attack, int defense, int satiation) {
        super(myName, represent, passable, myColor);
        this.hp = hp;
        this.damage = damage;
        this.attack = attack;
        this.defense = defense;
        this.satiation = satiation;
        this.feelings = new Vector<FeelingsObject>();
    }

    public int getDefense() {
        return defense;
    }

    public int getAttack() {
        return attack;
    }

    public int getHp() {
        return hp;
    }

    public void applyDamage(int hurts) {
        hp -= hurts;
    }

    public int getDamage() {
        return damage;
    }

    public int getSatiation() {
        return satiation;
    }

    public Boolean getWakeful() {
        return awake;
    }

    public void wakeUp() {
        awake = true;
    }

    public void goToSleep() {
        awake = false;
    }

    @Override
    public String objectOutput() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "MonsterObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + myColor.getColor() + eol + String.valueOf(visible) + eol 
            + hp + eol + damage + eol + attack + eol + defense + eol + satiation + eol 
            + x + eol + y + eol + String.valueOf(awake) + eol + eol;


//    private Vector<FeelingsObject> feelings = null; needs to be added when feelings are added
        return ret;
    }

    @Override
    public void pushObject(BufferedReader reader) {
        try {
            myName = reader.readLine();
            represent = reader.readLine().charAt(0);
            passable = reader.readLine().equalsIgnoreCase("true");
            myColor = new CSIColor(Integer.valueOf(reader.readLine()));
            visible = reader.readLine().equalsIgnoreCase("true");
            hp = Integer.valueOf(reader.readLine());
            damage = Integer.valueOf(reader.readLine());
            attack = Integer.valueOf(reader.readLine());
            defense = Integer.valueOf(reader.readLine());
            satiation = Integer.valueOf(reader.readLine());
            x = Integer.valueOf(reader.readLine());
            y = Integer.valueOf(reader.readLine());
            awake = reader.readLine().equalsIgnoreCase("true");
        } catch (IOException ioe) {
            System.out.println("Fatal error reading from file!");
            ioe.printStackTrace();
            return;
        }
    }
}
