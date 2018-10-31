package behavior.util.rmstate;

import behavior.plugin.analyzer.RMAnalyzer;
import behavior.util.rmconstants.StateConstants;

public class FirstMissingState implements RMState{
	private static RMState singleton = new FirstMissingState();
	
	public static RMState getInstance(){
		return singleton;
	}

	public void intake(RMROI roi){}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){
		RMAnalyzer.addEpisode(StateConstants.REFERENCE_MEMORY_ERROR);
		roi.changeState(EnterReferenceMemoryErrorState.getInstance());
	}

	public void exit(RMROI roi){
		RMAnalyzer.addEpisode(StateConstants.REFERENCE_MEMORY_ERROR);
		roi.changeState(OutAfterReferenceState.getInstance());
	}
}