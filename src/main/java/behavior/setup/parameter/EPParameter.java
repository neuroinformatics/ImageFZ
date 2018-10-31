package behavior.setup.parameter;

import behavior.setup.parameter.variable.EPBooleanVariable;
import behavior.setup.parameter.variable.IntVariable;

public class EPParameter extends Parameter{
	public static int openArmLocation;

	protected EPParameter(){}

	protected int setupNumber(int i){
		rate = i;
		var[i++] = new IntVariable("rate", "rate(frames/sec): ", 2);
		duration = i;
		var[i++] = new IntVariable("duration", "duration(sec): ", 600);
		binDuration = i;
		var[i++] = new IntVariable("bDuration", "bin duration(sec): ", 60);
		minSize = i;
		var[i++] = new IntVariable("subject.size.min", "subject size - min(pixels): ", 50);
		maxSize = i;
		var[i++] = new IntVariable("subject.size.max", "subject size - max(pixels): ", 999999);
		frameWidth = i;
		var[i++] = new IntVariable("frame.width", "frame size - width(cm): ", 55);
		frameHeight = i;
		var[i++] = new IntVariable("frame.height", "frame size - height(cm): ", 55);

		return i;
	}

	@Override
	protected int setupSubNumber(int i) {
		return i;
	}

	protected int setupSubCheckbox(int i) {
		openArmLocation = i;
		var[i++] = new EPBooleanVariable("open.arm.location", "OpenArmLocation :", true);
		return i;
	}
}