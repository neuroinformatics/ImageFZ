package behavior.plugin;

import behavior.plugin.executer.SIExecuter;

public class SIOnline extends BehaviorEntryPoint {
	public void run(String arg0){
		new SIExecuter().run();
	}
}
