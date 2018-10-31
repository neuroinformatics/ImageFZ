package behavior.plugin;

import behavior.plugin.executer.CSIExecuter;

public class CSIOnline extends BehaviorEntryPoint {
	public void run(String arg0){
		new CSIExecuter().run();
	}
}
