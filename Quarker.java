/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.quarker;

/**
 *
 * @author ehoward
 */
public class Quarker {

    public static void main(String args[]) {
        System.gc(); //added so that starting a new game right away doesn't leave artifacts
        new Workhorse();
    }
}