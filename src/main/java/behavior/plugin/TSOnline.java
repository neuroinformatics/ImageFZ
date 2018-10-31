package behavior.plugin;

import behavior.plugin.executer.TSExecuter;

public class TSOnline extends BehaviorEntryPoint{
	public void run(String arg0) {
		new TSExecuter(Integer.parseInt(arg0)).run();
	}
}