package behavior.plugin;

import behavior.plugin.executer.YMExecuter;

public class YMOnline extends BehaviorEntryPoint {
	public void run(String arg0) {
		new YMExecuter().run();
	}
}