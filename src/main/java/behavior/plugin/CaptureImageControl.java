package behavior.plugin;

import behavior.plugin.executer.ImageControlExecuter;

public class CaptureImageControl extends BehaviorEntryPoint {
	public void run(String arg0){
		new ImageControlExecuter(Integer.parseInt(arg0)).run();
	}
}
