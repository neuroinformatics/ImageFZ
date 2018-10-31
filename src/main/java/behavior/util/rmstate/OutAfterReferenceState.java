package behavior.util.rmstate;

import behavior.plugin.analyzer.RMAnalyzer;
import behavior.util.rmconstants.StateConstants;

public class OutAfterReferenceState implements RMState{
	private static RMState singleton = new OutAfterReferenceState();

	public static RMState getInstance(){
		return singleton;
	}

	public void exit(){}

	public void intake(RMROI roi){}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){
		RMAnalyzer.addEpisode(StateConstants.DOUBLE_ERROR);
		roi.changeState(EnterAfterReferenceMemoryErrorState.getInstance());	
	}

	public void exit(RMROI roi){}
}