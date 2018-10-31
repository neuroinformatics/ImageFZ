package behavior.plugin;

import behavior.plugin.executer.LDExecuter;

public class LDOnline extends BehaviorEntryPoint {
	public void run(String arg0){
		new LDExecuter(Integer.parseInt(arg0)).run();
	}
}
