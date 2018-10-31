package behavior.plugin;

import behavior.plugin.executer.EPExecuter;

public class EPOnline extends BehaviorEntryPoint{
	public void run(String arg0){
		new EPExecuter().run();
	}
}