package behavior.plugin;

import behavior.plugin.executer.EPoffExecuter;

public class EPOffline extends BehaviorEntryPoint {
	public void run(String arg0){
		new EPoffExecuter().run();
	}
}