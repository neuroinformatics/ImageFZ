package behavior.plugin;

import behavior.plugin.executer.LDoffExecuter;

public class LDOffline extends BehaviorEntryPoint {
	public void run(String arg0){
		new LDoffExecuter().run();
	}
}
