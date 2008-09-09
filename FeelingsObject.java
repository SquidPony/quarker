/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package my.quarker;

/**
 *
 * @author ehoward
 */
public class FeelingsObject {
    private AttitudeEnum feelingStatus;
    private GameObject feelingTowards;
    
    public FeelingsObject(){
        feelingStatus = null;
        feelingTowards = null;
    }
    
    public FeelingsObject(AttitudeEnum status, GameObject towards){
        feelingStatus = status;
        feelingTowards = towards;
    }
    
    public AttitudeEnum GetFeelingStatus(){
        return feelingStatus;
    }
    
    public GameObject GetFeelingTowards(){
        return feelingTowards;
    }
    
    public void SetFeelingStatus(AttitudeEnum status){
        feelingStatus = status;
    }
    
    public void SetFeelingTowards(GameObject towards){
        feelingTowards = towards;
    }
}
