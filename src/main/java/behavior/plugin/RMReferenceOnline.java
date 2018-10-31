package behavior.plugin;

import behavior.plugin.executer.RMExecuter;
import behavior.util.rmconstants.RMConstants;

public class RMReferenceOnline extends BehaviorEntryPoint {

	public void run(String arg0){
		RMConstants.setReferenceMemoryMode(true);
        new RMExecuter().run();
	}
}