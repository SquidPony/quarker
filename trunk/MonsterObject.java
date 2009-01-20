/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

import java.util.Vector;
import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author Eben
 */
public class MonsterObject extends BaseObject {

    protected int mass, //current health of the monster
             damage, //amount of base damage the monster will do
             aim,  agility,  level,  penetration,  toughness,  attackPower,  deflection,  satiation, //how full player will get from absorbing the monster
             x,  y; //current x,y coordinates for the monster
    protected Vector<FeelingsObject> feelings = null;
    protected boolean awake = false; //will tell us if the monster is active, default is not active

    MonsterObject() {
        super();
    }

    public MonsterObject(String myName, char represent, boolean passable,
            CSIColor frontColor, CSIColor backColor, int hp, int damage, int attack,
            int aim, int agility, int level, int penetration, int toughness,
            int attackPower, int deflection,
            int defense, int satiation) {

        this.myName = myName;
        this.represent = represent;
        this.passable = passable;
        this.mass = hp;
        this.damage = damage;
        this.satiation = satiation;
        this.frontColor = frontColor;
        this.backColor = backColor;
        this.feelings = new Vector<FeelingsObject>();

    }

    public void deepCopy(MonsterObject mon) {
        deepCopy((BaseObject) mon);
        mass = mon.mass;
        damage = mon.damage;
        aim = mon.aim;
        agility = mon.agility;
        level = mon.level;
        penetration = mon.penetration;
        toughness = mon.toughness;
        attackPower = mon.attackPower;
        deflection = mon.deflection;
        satiation = mon.satiation;
    }

    public int getAgility() {
        return agility;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public boolean isAwake() {
        return awake;
    }

    public void setAwake(boolean awake) {
        this.awake = awake;
    }

    public int getDeflection() {
        return deflection;
    }

    public void setDeflection(int deflection) {
        this.deflection = deflection;
    }

    public Vector<FeelingsObject> getFeelings() {
        return feelings;
    }

    public void setFeelings(Vector<FeelingsObject> feelings) {
        this.feelings = feelings;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMass() {
        return mass;
    }

    public void setMass(int mass) {
        this.mass = mass;
    }

    public int getPenetration() {
        return penetration;
    }

    public void setPenetration(int penetration) {
        this.penetration = penetration;
    }

    public int getToughness() {
        return toughness;
    }

    public void setToughness(int toughness) {
        this.toughness = toughness;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getAim() {
        return aim;
    }

    public void setAim(int aim) {
        this.aim = aim;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getSatiation() {
        return satiation;
    }

    public void setSatiation(int satiation) {
        this.satiation = satiation;
    }

    public void applyDamage(int hurts) {
        mass -= hurts;
    }

    public void wakeUp() {
        awake = true;
    }

    public void goToSleep() {
        awake = false;
    }
}
