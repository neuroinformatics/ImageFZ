package behavior.plugin;

import behavior.plugin.executer.FZExecuter;

public class FZOnline extends BehaviorEntryPoint{
	public void run(String arg0){
		new FZExecuter(Integer.parseInt(arg0)).run();
	}
}