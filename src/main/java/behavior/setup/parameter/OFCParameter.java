package behavior.setup.parameter;

import behavior.setup.parameter.variable.BooleanVariable;
import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntVariable;

public class OFCParameter extends OFParameter{
	public static int angleMargin;
	public static int resetDist;
	public static int resetImmobilityTime;
	public static int roiRadius;
	public static int checkMode;
	public static int checkModeSave;

	protected int setupSubNumber(int i){
		i = super.setupSubNumber(i);
		angleMargin = i;
		var[i++] = new IntVariable("angle.margin", "angle margin(degree): ", 30);
		resetDist = i;
		var[i++] = new DoubleVariable("reset.dist", "reset dist(cm): ", 150);
		resetImmobilityTime = i;
		var[i++] = new IntVariable("reset.immobility.time", "reset immobility time(sec): ", 5);
		roiRadius = i;
		var[i++] = new DoubleVariable("roi.radius", "roi radius(cm): ", 12);
		return i;
	}
	
	protected int setupSubCheckbox(int i){
		checkMode = i;
		var[i++] = new BooleanVariable("mode.check", "check mode", false);
		checkModeSave = i;
		var[i++] = new BooleanVariable("mode.check.save", "check mode save", false);
		return i;
	}
}