package behavior.setup.parameter;

import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntVariable;

public class FZShockParameter extends Parameter{
	public static int centerArea;
	public static int movementCriterion;
	public static int shockCaptureRate;

	@Override
	protected int setupNumber(int i){
		shockCaptureRate = i;
		var[i++] = new IntVariable("shock.capture.rate", "shock rate(frame/sec): ", 4);
		duration = i;
		var[i++] = new IntVariable("duration", "duration(sec): ", 600);
		binDuration = i;
		var[i++] = new IntVariable("bDuration", "bin duration(sec): ", 60);
		minSize = i;
		var[i++] = new IntVariable("subject.size.min", "subject size - min(pixels): ", 0);
		maxSize = i;
		var[i++] = new IntVariable("subject.size.max", "subject size - max(pixels): ", 999999);
		frameWidth = i;
		var[i++] = new IntVariable("frame.width", "frame size - width(cm): ", 0);
		frameHeight = i;
		var[i++] = new IntVariable("frame.height", "frame size - height(cm): ", 0);
		i = setupSubNumber(i);
		return i;
	}

	@Override
	protected int setupSubNumber(int i){
		centerArea = i;
		var[i++] = new IntVariable("center.area", "center.area(%): ", 100);
		movementCriterion = i;
		var[i++] = new DoubleVariable("movement.criterion", "movement criterion(cm/sec): ", 1.0);
		//rate = i;
		//var[i++] = new FZIntVariable("rate", "rate(frames/sec): ", 1);
		return i;
	}

	@Override
	protected int setupSubCheckbox(int i){return i;}
}