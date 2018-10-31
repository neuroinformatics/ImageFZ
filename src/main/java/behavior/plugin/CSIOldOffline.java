package behavior.plugin;

import behavior.plugin.executer.CSIOldOffExecuter;

public class CSIOldOffline extends BehaviorEntryPoint{
	public void run(String arg0){
		new CSIOldOffExecuter().run();
	}
}