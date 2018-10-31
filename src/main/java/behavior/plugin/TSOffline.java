package behavior.plugin;

import behavior.plugin.executer.TSoffExecuter;

public class TSOffline extends BehaviorEntryPoint{
	public void run(String arg0) {
		new TSoffExecuter().run();	
	}
}
