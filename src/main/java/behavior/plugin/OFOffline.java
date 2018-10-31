package behavior.plugin;

import behavior.plugin.executer.OFoffExecuter;

public class OFOffline extends BehaviorEntryPoint {
	public void run(String arg0){
		new OFoffExecuter().run();
	}
}
