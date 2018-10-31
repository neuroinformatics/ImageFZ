package behavior.plugin;

import behavior.plugin.executer.RMOfflineExecuter;
import behavior.util.rmconstants.RMConstants;

public class RMWorkingOffline extends BehaviorEntryPoint {

	public void run(String arg0){
		RMConstants.setReferenceMemoryMode(false);
		RMConstants.setFoodArmNum(RMConstants.ARM_NUM);
        new RMOfflineExecuter().run();
	}
}