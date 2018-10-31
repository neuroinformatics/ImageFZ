package behavior.plugin;

import behavior.plugin.executer.BToffExecuter;

public class BTOffline extends BehaviorEntryPoint {
	public void run(String arg0){
		new BToffExecuter().run();
	}
}