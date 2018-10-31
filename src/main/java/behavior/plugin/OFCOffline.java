package behavior.plugin;

import behavior.plugin.executer.OFCoffExecuter;

public class OFCOffline extends BehaviorEntryPoint {
	public void run(String arg0){
		new OFCoffExecuter().run();
	}
}
