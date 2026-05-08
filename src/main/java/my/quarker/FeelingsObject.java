package my.quarker;
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
