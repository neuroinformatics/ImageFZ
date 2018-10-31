package behavior.plugin;

import behavior.plugin.executer.TMoffExecuter;

public class TMOffline extends BehaviorEntryPoint{
	public void run(String arg0) {
		new TMoffExecuter().run();
	}
}