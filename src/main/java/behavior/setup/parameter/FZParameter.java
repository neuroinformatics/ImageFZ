package behavior.setup.parameter;

import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntSliderVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.ThresholdBooleanVariable;

public class FZParameter extends Parameter{
	public static int shockCaptureRate;
	public static int freezCriterion;
	public static int minFreezDuration;

	@Override
	protected int setupSlider(int i){
		minThreshold = i;
		var[i++] = new IntSliderVariable("threshold.min", "threshold min", 0, 0, 255);
		xorThreshold = i;
		var[i++] = new IntSliderVariable("xor.threshold.min", "threshold min (xor)", 150, 0, 255);
		maxThreshold = i;
		var[i++] = new IntSliderVariable("threshold.max", "threshold max", 255, 0, 255);
		return i;
	}

	@Override
	protected int setupNumber(int i){
		rate = i;
		var[i++] = new IntVariable("rate", "rate(frames/sec): ", 1);
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
		freezCriterion = i;
		var[i++] = new IntVariable("freezing.criterion", "freezing criterion(pixel): ", 20);
		minFreezDuration = i;
		var[i++] = new DoubleVariable("freez.duration.min", "freezing duration -min(sec): ", 2.0);
		return i;
	}

	@Override
	protected int setupSubCheckbox(int i){
		shockCaptureRate = i;
		var[i++] = new IntVariable("shock.capture.rate", "shock rate(frame/sec): ", 4);
		subtractBackground = i;
		var[i++] = new ThresholdBooleanVariable("subtract.background", "subtract xor background", false);
		erode = i;
		var[i++] = new ThresholdBooleanVariable("erode", "erode", false);
		return i;
	}
}