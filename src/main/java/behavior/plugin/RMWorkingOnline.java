package behavior.plugin;

import behavior.plugin.executer.RMExecuter;
import behavior.util.rmconstants.RMConstants;

public class RMWorkingOnline extends BehaviorEntryPoint {

	public void run(String arg0){
		RMConstants.setReferenceMemoryMode(false);
		RMConstants.setFoodArmNum(RMConstants.ARM_NUM);
        new RMExecuter().run();
	}
}