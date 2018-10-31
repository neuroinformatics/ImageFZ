package behavior.plugin;

import behavior.plugin.executer.RMOfflineExecuter;
import behavior.util.rmconstants.RMConstants;

public class RMReferenceOffline extends BehaviorEntryPoint {

	public void run(String arg0){
		RMConstants.setReferenceMemoryMode(true);
        new RMOfflineExecuter().run();
	}
}