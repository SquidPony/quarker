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
public enum MonsterTypeEnum {
    TRUTH("truth quark",'t',false,CSIColor.MAGENTA,100,100,50,50,10), 
    BEAUTY("beauty quark",'b',false,CSIColor.GREEN,100,50,100,50,10),
    CHARM("charm quark",'c',false,CSIColor.GRAY,50,50,25,20,5),
    STRANGE("strange quark",'s',false,CSIColor.CYAN,50,25,50,20,5),
    UP("up quark",'u',false,CSIColor.YELLOW,5,2,1,2,1),
    DOWN("down quark",'d',false,CSIColor.BLUE,5,1,2,2,1),
    ANTIUP("antiup quark", 'u',false,CSIColor.YELLOW,5,2,1,2,1);
    
    String myName;
    char represent;
    Boolean passable;
    CSIColor myColor;
    int hp,damage,attack,defense,satiation;
                
    private MonsterTypeEnum(String myName, char represent, Boolean passable, CSIColor myColor, int hp, int damage, int attack, int defense, int satiation){
        this.myName = myName;
        this.represent = represent;
        this.passable = passable;
        this.myColor = myColor;
        this.hp = hp;
        this.damage = damage;
        this.attack = attack;
        this.defense = defense;
        this.satiation = satiation;
    }
    
    public MonsterTypeEnum figureType(String myName){
        if (myName.equals("truth quark")){
            return TRUTH;
        } else if (myName.equals("beauty quark")){
            return BEAUTY;
        } else if (myName.equals("charm quark")){
            return CHARM;
        } else if (myName.equals("strange quark")){
            return STRANGE;
        } else if (myName.equals("up quark")){
            return UP;
        } else if (myName.equals("down quark")){
            return DOWN;
        }
        return null;
    }
}
