package behavior.util.rmstate;

import behavior.plugin.analyzer.RMAnalyzer;
import behavior.util.rmconstants.StateConstants;

public class EnterAfterOmissionState implements RMState {
	private static RMState singleton = new EnterAfterOmissionState();

	public static RMState getInstance(){
		return singleton;
	}

	public void intake(RMROI roi) {
		RMAnalyzer.addEpisode(StateConstants.INTAKE_AFTER_OMISSION);
		roi.changeState(EnterIntakeState.getInstance());
	}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){}
 
	public void exit(RMROI roi){
		RMAnalyzer.addEpisode(StateConstants.OMISSION_AFTER_OMISSION);
		roi.changeState(OutState.getInstance());
	}
}
