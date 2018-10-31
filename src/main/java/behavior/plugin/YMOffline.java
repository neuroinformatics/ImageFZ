package behavior.plugin;

import behavior.plugin.executer.YMoffExecuter;

public class YMOffline extends BehaviorEntryPoint{
	public void run(String arg0) {
		new YMoffExecuter().run();
	}
}