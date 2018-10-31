package behavior.plugin;

import behavior.plugin.executer.CSIReferenceSettingExecuter;

public class CSIReferenceSetting extends BehaviorEntryPoint{
	public void run(String arg0){
        new CSIReferenceSettingExecuter().run();
	}
}
