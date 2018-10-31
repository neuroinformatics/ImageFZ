package behavior.util.rmstate;

import behavior.plugin.analyzer.RMAnalyzer;
import behavior.util.rmconstants.StateConstants;

public class OutAfterIntakeState implements RMState{
	private static RMState singleton = new OutAfterIntakeState();
	
	public static RMState getInstance(){
		return singleton;
	}

	public void exit(){}

	public void intake(RMROI roi){}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){
		RMAnalyzer.addEpisode(StateConstants.WORKING_MEMORY_ERROR);
		roi.changeState(EnterWorkingMemoryErrorState.getInstance());	
	}

	public void exit(RMROI roi){}
}