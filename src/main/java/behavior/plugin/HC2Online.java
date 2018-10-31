package behavior.plugin;

import behavior.plugin.executer.HC2Executer;

public class HC2Online extends BehaviorEntryPoint{
	public void run(String arg0){
		new HC2Executer(Integer.parseInt(arg0)).run();
	}
}
