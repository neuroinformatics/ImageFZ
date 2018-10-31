package behavior.plugin;


import behavior.setup.BMReferenceSetup;
import ij.IJ;

public class BMRefSetup extends BehaviorEntryPoint{
	public void run(String arg) {
		new BMReferenceSetup(IJ.getInstance());
	}
}
