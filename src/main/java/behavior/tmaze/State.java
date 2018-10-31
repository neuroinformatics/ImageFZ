package behavior.tmaze;

import java.util.HashMap;

public class State{
    private HashMap<String,String> trigger = new HashMap<String,String>();

    public State(String triggerRoi,String nextState){
    	 trigger.put(triggerRoi,nextState);
    }

    public void addTrigger(String triggerRoi,String nextState){
    	trigger.put(triggerRoi,nextState);
    }

    public String fireTrigger(String roiNo){
    	if(trigger.containsKey(roiNo)){
    	    return trigger.get(roiNo);
    	}else{
    		return "";
    	}
    }
}