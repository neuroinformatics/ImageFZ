package behavior.plugin;

import behavior.plugin.executer.BMExecuter;

public class BMOnline extends BehaviorEntryPoint{
		public void run(String arg0){
			new BMExecuter().run();
		}
	}

