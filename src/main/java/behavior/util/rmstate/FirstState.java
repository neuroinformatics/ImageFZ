package behavior.util.rmstate;

import behavior.plugin.analyzer.RMAnalyzer;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.RMParameter;
import behavior.util.rmconstants.RMConstants;
import behavior.util.rmconstants.StateConstants;

public class FirstState implements RMState {
	private static RMState singleton = new FirstState();
	
	public static RMState getInstance(){
		return singleton;
	}

	public void intake(RMROI roi){
		RMAnalyzer.addEpisode(StateConstants.INTAKE);
		roi.changeState(EnterIntakeState.getInstance());
	}

	public void missing(RMROI roi){
		roi.changeState(FirstMissingState.getInstance());
	}

	public void enter(RMROI roi){
		if(RMConstants.isOffline() || Parameter.getBoolean(RMParameter.NSense)){
			RMAnalyzer.addEpisode(StateConstants.INTAKE);
		    roi.changeState(EnterIntakeState.getInstance());
		    return;
		}

		roi.changeState(EnterState.getInstance());	
	}

	public void exit(RMROI roi){}
}