/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

import net.slashie.libjcsi.CSIColor;

/**
 *
 * @author ehoward
 */
public class PlayerObject extends MonsterObject {
    private int maxHp = 40;
    private int size = 1;
    private int nextSize = 8;
    private int viewRange = 5;

    public PlayerObject() {
        myName = "Player";
        represent = '@';
        passable = true;
        frontColor = CSIColor.WHITE;
    }

    public void initialize(){
        setMass(maxHp);
        setLevel(1);
        setDamage(5);
        setAim(5);
        setAgility(5);
        setPenetration(5);
        setToughness(5);
        setAttackPower(5);
        setDeflection(5);
    }

    public void levelUp(){
        maxHp +=10;
        damage +=2;
        aim +=3;
        agility +=3;
        penetration +=2;
        toughness+=2;
        attackPower+=3;
        deflection+=2;
    }

    public void deepCopy(PlayerObject obj){
        deepCopy((MonsterObject)obj);
        maxHp = obj.maxHp;
        size = obj.size;
        nextSize = obj.nextSize;
        viewRange = obj.viewRange;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNextSize() {
        return nextSize;
    }

    public void setNextSize(int nextSize) {
        this.nextSize = nextSize;
    }

    public int getViewRange() {
        return viewRange;
    }

    public void setViewRange(int viewRange) {
        this.viewRange = viewRange;
    }
}