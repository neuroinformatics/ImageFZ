package behavior.plugin;

import behavior.plugin.executer.RMReferenceSettingExecuter;

public class RMReferenceSetting extends BehaviorEntryPoint {

	public void run(String arg0){
        new RMReferenceSettingExecuter().run();
	}
}