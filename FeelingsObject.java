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
    private BaseObject feelingTowards;
    
    public FeelingsObject(){
        feelingStatus = null;
        feelingTowards = null;
    }
    
    public FeelingsObject(AttitudeEnum status, BaseObject towards){
        feelingStatus = status;
        feelingTowards = towards;
    }
    
    public AttitudeEnum GetFeelingStatus(){
        return feelingStatus;
    }
    
    public BaseObject GetFeelingTowards(){
        return feelingTowards;
    }
    
    public void SetFeelingStatus(AttitudeEnum status){
        feelingStatus = status;
    }
    
    public void SetFeelingTowards(BaseObject towards){
        feelingTowards = towards;
    }
}
