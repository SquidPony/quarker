package my.quarker;

/**
 *
 * @author ehoward
 */
public class QuarkObject extends MonsterObject{
    public static final QuarkObject TRUTH = new QuarkObject("truth quark", 't', false, 100, 100, 50, 50, 10),  BEAUTY = new QuarkObject("beauty quark", 'b', false, 100, 50, 100, 50, 10),  CHARM = new QuarkObject("charm quark", 'c', false, 50, 50, 25, 20, 5),  STRANGE = new QuarkObject("strange quark", 's', false, 50, 25, 50, 20, 5),  UP = new QuarkObject("up quark", 'u', false, 5, 2, 1, 2, 1),  DOWN = new QuarkObject("down quark", 'd', false, 5, 1, 2, 2, 1),  
        ANTIUP = new QuarkObject("antiup quark", 'u', false, 5, 2, 1, 2, 1);
    private static final int RED = 0,  GREEN = 1,  BLUE = 2,  ANTIRED = 3,  ANTIGREEN = 4,  ANTIBLUE = 5;

}
