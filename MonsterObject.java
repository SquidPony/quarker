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

    protected int mass, //current health of the monster
         damage, //amount of base damage the monster will do
         attack, //base chance of hit on attack
         defense, //base defense against attack
         satiation, //how full player will get from absorbing the monster
         x,  y; //current x,y coordinates for the monster
    protected Vector<FeelingsObject> feelings = null;
    protected boolean awake = false; //will tell us if the monster is active, default is not active

    MonsterObject() {
        super();
    }

    public MonsterObject(MonsterObject mon) {
        myName = mon.myName;
        represent = mon.represent;
        passable = mon.passable;
        frontColor = mon.frontColor;
        backColor = mon.backColor;
        mass = mon.mass;
        damage = mon.damage;
        attack = mon.attack;
        defense = mon.defense;
        satiation = mon.satiation;
    }

    public MonsterObject(String myName, char represent, boolean passable, 
        CSIColor frontColor, CSIColor backColor, int hp, int damage, int attack, 
        int defense, int satiation){
        
        this.myName = myName;
        this.represent = represent;
        this.passable = passable;
        this.mass = hp;
        this.damage = damage;
        this.attack = attack;
        this.defense = defense;
        this.satiation = satiation;
        this.frontColor = frontColor;
        this.backColor = backColor;
        this.feelings = new Vector<FeelingsObject>();

    }

    public int getDefense() {
        return defense;
    }

    public int getAttack() {
        return attack;
    }

    public int getHp() {
        return mass;
    }

    public void applyDamage(int hurts) {
        mass -= hurts;
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
    public String outputObjectToFile() {// this should be overridden to ensure everything is saved correctly
        String ret = "";
        String eol = System.getProperty("line.separator");
        ret = "MonsterObject" + eol + myName + eol + String.valueOf(represent) + eol + String.valueOf(passable) + eol + frontColor.getColor() + eol + String.valueOf(visible) + eol + mass + eol + damage + eol + attack + eol + defense + eol + satiation + eol + x + eol + y + eol + String.valueOf(awake) + eol + eol;


//    private Vector<FeelingsObject> feelings = null; needs to be added when feelings are added
        return ret;
    }

    @Override
    public void inputObjectFromFile(BufferedReader reader) {
        try {
            myName = reader.readLine();
            represent = reader.readLine().charAt(0);
            passable = reader.readLine().equalsIgnoreCase("true");
            frontColor = new CSIColor(Integer.valueOf(reader.readLine()));
            visible = reader.readLine().equalsIgnoreCase("true");
            mass = Integer.valueOf(reader.readLine());
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
