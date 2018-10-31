package behavior.plugin;

import behavior.plugin.executer.OFExecuter;

public class OFOnline extends BehaviorEntryPoint {
	public void run(String arg0){
		new OFExecuter(Integer.parseInt(arg0)).run();
	}
}
