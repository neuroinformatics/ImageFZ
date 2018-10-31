package behavior.plugin;

import behavior.plugin.executer.FZoffExecuter;

public class FZOffline extends BehaviorEntryPoint {
	public void run(String arg0){
		new FZoffExecuter().run();
	}
}
