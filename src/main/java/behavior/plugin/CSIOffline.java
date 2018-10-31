package behavior.plugin;

import behavior.plugin.executer.CSIoffExecuter;

public class CSIOffline extends BehaviorEntryPoint{
	public void run(String arg0){
		new CSIoffExecuter().run();
	}
}
