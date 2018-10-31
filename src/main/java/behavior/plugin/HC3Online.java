package behavior.plugin;

import behavior.plugin.executer.HC3Executer;

public class HC3Online extends BehaviorEntryPoint{
	public void run(String arg0){
		new HC3Executer(Integer.parseInt(arg0)).run();
	}
}
