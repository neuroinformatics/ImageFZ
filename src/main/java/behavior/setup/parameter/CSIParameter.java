package behavior.setup.parameter;

import behavior.setup.parameter.variable.BTIntVariable;
import behavior.setup.parameter.variable.CSIBooleanVariable;
import behavior.setup.parameter.variable.CSIDoubleVariable;
import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntVariable;

public class CSIParameter extends Parameter{
	public static int innerDiameter;
	public static int hysDist;
	public static int mountDensity;
	public static int leftSeparator;
	public static int rightSeparator;
	public static int movementCriterion;
	public static int useMask;
	public static final int UNSET = -1;

	protected CSIParameter(){}

	@Override
	protected int setupNumber(int i){
		rate = i;
		var[i++] = new IntVariable("rate", "rate(frames/sec): ", 1);
		duration = i;
		var[i++] = new IntVariable("duration", "duration(sec): ", 600);
		binDuration = i;
		var[i++] = new IntVariable("bDuration", "bin duration(sec): ", 60);
		minSize = i;
		var[i++] = new IntVariable("subject.size.min", "subject size - min(pixels): ", 50);
		maxSize = i;
		var[i++] = new IntVariable("subject.size.max", "subject size - max(pixels): ", 999999);
		frameWidth = i;
		var[i++] = new IntVariable("frame.width", "frame size - width(cm): ", 50);
		frameHeight = i;
		var[i++] = new IntVariable("frame.height", "frame size - height(cm): ", 38);
		i = setupSubNumber(i);

		return i;
	}

	@Override
	protected int setupSubNumber(int i){		
		innerDiameter = i;
		var[i++] = new CSIDoubleVariable("diameter.inner","",UNSET);
		leftSeparator = i;
		var[i++] = new BTIntVariable("separator.left","",UNSET);
		rightSeparator = i;
		var[i++] = new BTIntVariable("separator.right","",UNSET);
		movementCriterion = i;
		var[i++] = new DoubleVariable("moving.criterion", "movement Criterion(cm/s): ", 1.5);
		mountDensity = i;
		var[i++] = new IntVariable("mountgraph.value","mountgraph.value:",1);

		hysDist = i;
		var[i++] = new CSIDoubleVariable("distance.hysteresis","",UNSET);
		useMask = i;
		var[i++] = new CSIBooleanVariable("mask","",true);
		return i;
	}

	@Override
	protected int setupSubCheckbox(int i){return i;}
}