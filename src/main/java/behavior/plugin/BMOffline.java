package behavior.plugin;

import behavior.plugin.executer.BMoffexecuter;

public class BMOffline extends BehaviorEntryPoint {

	public void run(String arg0) {
		new BMoffexecuter().run();
	}

}
