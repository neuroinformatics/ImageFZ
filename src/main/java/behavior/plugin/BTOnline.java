package behavior.plugin;

import behavior.plugin.executer.BTExecuter;

public class BTOnline extends BehaviorEntryPoint {
	public void run (String args0) {
		//Cage����1�ŌŒ肷��
		new BTExecuter().run();
	}
}