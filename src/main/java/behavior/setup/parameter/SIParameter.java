package behavior.setup.parameter;

import behavior.setup.parameter.variable.*;

public class SIParameter extends Parameter{
	public static int contactDis;
	public static int minActDis;

	protected SIParameter(){}

	protected int setupNumber(int i){
		rate = i;
		var[i++] = new IntVariable("rate", "rate(frames/sec): ", 1);
		duration = i;
		var[i++] = new IntVariable("duration", "duration(sec): ", 600);
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

	protected int setupSubNumber(int i){
		contactDis = i;
		var[i++] = new DoubleVariable("contactDis","contactDis(cm): ",4);
		minActDis = i;
		var[i++] = new DoubleVariable("minActDis","minActDis(cm/sec): ",4);
		return i;
	}

	@Override
	protected int setupSubCheckbox(int i){return i;}
}







