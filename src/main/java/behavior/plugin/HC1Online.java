package behavior.plugin;

import behavior.plugin.executer.HC1Executer;

public class HC1Online extends BehaviorEntryPoint{
	public void run(String arg0){
		new HC1Executer(Integer.parseInt(arg0)).run();
	}
}
