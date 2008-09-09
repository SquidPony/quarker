/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package my.quarker;

/**
 *
 * @author ehoward
 */
public enum MyKeyBindings {
    N_KEY ('8'),
    NE_KEY ('9'),
    E_KEY ('6'),
    SE_KEY ('3'),
    S_KEY ('2'),
    SW_KEY ('1'),
    W_KEY ('4'),
    NW_KEY ('7'),
    REST_KEY ('5'),
    VERSION_KEY('V'),
    DOWN_KEY('>'),
    UP_KEY('<');
    
    public char binding;
    
    private MyKeyBindings(char binding){
        this.binding = binding;
    }
}
