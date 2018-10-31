package behavior.tmaze;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import behavior.gui.BehaviorDialog;

public class StateManager{
	public enum TestType{RIGHT,LEFT,FORCED_ALTERNATION,SPONTANEOUS_ALTERNATION}

	private TestType type;

	private String currentState;

	private int correctNum = 0;
	private int choiceRightNum = 0;
	private int choiceLeftNum = 0;

	private HashMap<String,State> stateSet;

    public StateManager(TestType type){
    	this.type = type;
    	try{
    	    switch(type){
    	        case RIGHT:
    		        stateSet = new MacroReader().getStateSet(new URL(this.getClass().getResource(""),"LR_Macro.txt"));
    		        break;
    	        case LEFT:
    	        	stateSet = new MacroReader().getStateSet(new URL(this.getClass().getResource(""),"LR_Macro.txt"));
    		        break;
    	        case FORCED_ALTERNATION:
    	       	    stateSet = new MacroReader().getStateSet(new URL(this.getClass().getResource(""),"FS_Macro.txt"));
    	       	    break;
    	        case SPONTANEOUS_ALTERNATION:
    	       	    stateSet = new MacroReader().getStateSet(new URL(this.getClass().getResource(""),"SA_Macro.txt"));
    	       	    break;
    	    }
    	    currentState = "ST1";
    	}catch(MalformedURLException e){
    		e.printStackTrace();
    		BehaviorDialog.showErrorDialog(e.toString());
    	}catch(Throwable e){
    		e.printStackTrace();
    		BehaviorDialog.showErrorDialog(e.toString());
    	}
    }

    public void fireTrigger(int cageNo){
    	String nextState = stateSet.get(currentState).fireTrigger(""+cageNo);
    	if(nextState.equals("")) return;
    	switch(type){
    	    case RIGHT:
    		    if(nextState.equals("RS1")){
    			    correctNum++;
    			    choiceRightNum++;
    		    }else if(nextState.equals("LS1")){
    			    choiceLeftNum++;
    		    }else if(currentState.equals("RS1") && nextState.equals("ST2")){
    		    	correctNum--;
    		    	choiceRightNum--;
    		    }else if(currentState.equals("LS1") && nextState.equals("ST2")){
    		    	choiceLeftNum--;
    		    }
	            break;
            case LEFT:
            	if(nextState.equals("RS1")){
    			    choiceRightNum++;
    		    }else if(nextState.equals("LS1")){
    		    	correctNum++;
    			    choiceLeftNum++;
    		    }else if(currentState.equals("RS1") && nextState.equals("ST2")){
    		    	choiceRightNum--;
    		    }else if(currentState.equals("LS1") && nextState.equals("ST2")){
    		    	correctNum--;
    		    	choiceLeftNum--;
    		    }
	            break;
            case SPONTANEOUS_ALTERNATION:
            	if(nextState.equals("RS_L1")){
            		correctNum++;
            		choiceRightNum++;
            	}else if(nextState.equals("LS_R1")){
            		correctNum++;
            		choiceLeftNum++;
            	}else if(nextState.equals("RS_R1")){
            		choiceRightNum++;
            	}else if(nextState.equals("LS_L1")){
            		choiceLeftNum++;
            	}else if(currentState.equals("RS_L1") && nextState.equals("ST_L2")){
            		correctNum--;
            		choiceRightNum--;
            	}else if(currentState.equals("LS_R1") && nextState.equals("ST_R2")){
            		correctNum--;
            		choiceLeftNum--;
            	}else if(currentState.equals("RS_R1") && nextState.equals("ST_R2")){
            		choiceRightNum--;
            	}else if(currentState.equals("LS_L1") && nextState.equals("ST_L2")){
            		choiceLeftNum--;
            	}
            	break;
            case FORCED_ALTERNATION:
            	if(nextState.equals("RS_L1")){
            		correctNum++;
            		choiceRightNum++;
            	}else if(nextState.equals("LS_R1")){
            		correctNum++;
            		choiceLeftNum++;
            	}else if(nextState.equals("RS_R1")){
            		choiceRightNum++;
            	}else if(nextState.equals("LS_L1")){
            		choiceLeftNum++;
            	}else if(currentState.equals("RS_L1") && nextState.equals("ST_L2")){
            		correctNum--;
            		choiceRightNum--;
            	}else if(currentState.equals("LS_R1") && nextState.equals("ST_R2")){
            		correctNum--;
            		choiceLeftNum--;
            	}else if(currentState.equals("RS_R1") && nextState.equals("ST_R2")){
            		choiceRightNum--;
            	}else if(currentState.equals("LS_L1") && nextState.equals("ST_L2")){
            		choiceLeftNum--;
            	}
            	break;
    	}

    	currentState = nextState;
    }

    public int getCorrectNum(){
    	return correctNum;
    }

    public int getChoiceRightNum(){
    	return choiceRightNum;
    }

    public int getChoiceLeftNum(){
    	return choiceLeftNum;
    }
}