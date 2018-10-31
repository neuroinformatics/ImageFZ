package behavior.setup.parameter;

import behavior.setup.parameter.variable.CSIDoubleVariable;
import behavior.setup.parameter.variable.IntVariable;

public class YMParameter extends Parameter{
	public static int innerDiameter;
	public static int hysDist;
	public static int openArmLocation;
	public static final int UNSET = -1;

	protected YMParameter(){}

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
		var[i++] = new IntVariable("frame.width", "frame size - width(cm): ", 84);
		frameHeight = i;
		var[i++] = new IntVariable("frame.height", "frame size - height(cm): ", 84);

		innerDiameter = i;
		var[i++] = new CSIDoubleVariable("diameter.inner","",UNSET);
		hysDist = i;
		var[i++] = new CSIDoubleVariable("distance.hysteresis","",UNSET);

		return i;
	}

	@Override
	protected int setupSubCheckbox(int i){return i;}
	protected int setupSubNumber(int i){return i;}
}