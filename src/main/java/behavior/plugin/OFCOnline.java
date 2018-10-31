package behavior.plugin;

import behavior.plugin.executer.OFCExecuter;

public class OFCOnline extends BehaviorEntryPoint {
	public void run(String arg0){
		new OFCExecuter(Integer.parseInt(arg0)).run();
	}
}
