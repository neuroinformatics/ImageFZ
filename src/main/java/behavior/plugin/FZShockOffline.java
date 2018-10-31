package behavior.plugin;

import behavior.plugin.executer.FZShockOffExecuter;

public class FZShockOffline extends BehaviorEntryPoint {
	public void run(String arg0){
		new FZShockOffExecuter().run();
	}
}
