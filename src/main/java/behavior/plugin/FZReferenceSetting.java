package behavior.plugin;

import behavior.plugin.executer.FZReferenceSettingExecuter;

public class FZReferenceSetting extends BehaviorEntryPoint {
	public void run(String arg0) {
		new FZReferenceSettingExecuter().run();
	}
}