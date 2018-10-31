package behavior.util.rmstate;

import behavior.plugin.analyzer.RMAnalyzer;
import behavior.util.rmconstants.StateConstants;

public class EnterState implements RMState {

	private static RMState singleton = new EnterState();
	
	public static RMState getInstance(){
		return singleton;
	}

	public void intake(RMROI roi){
		RMAnalyzer.addEpisode(StateConstants.INTAKE);
		roi.changeState(EnterIntakeState.getInstance());
	}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){}

	public void exit(RMROI roi){
        RMAnalyzer.addEpisode(StateConstants.OMISSION);
		roi.changeState(OutState.getInstance());
	}
}