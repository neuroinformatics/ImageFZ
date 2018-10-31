package behavior.plugin;

import behavior.plugin.executer.SIoffExecuter;

public class SIOffline extends BehaviorEntryPoint{
	public void run(String arg0){
		new SIoffExecuter().run();
	}
}
