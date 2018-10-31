package behavior.setup.parameter;

import behavior.setup.parameter.variable.HCIntVariable1;
import behavior.setup.parameter.variable.HCIntVariable2;
import behavior.setup.parameter.variable.IntSliderVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.ThresholdBooleanVariable;
import behavior.setup.parameter.variable.ThresholdIntVariable;
import behavior.setup.parameter.variable.Variable;

public class HCParameter extends Parameter {
	public static int luminanceThreshold;
	public static int LightON, LightOFF;
	public static int reduceTimes, dilateTimes;

	protected HCParameter(){
		var = new Variable[MAX_VARIABLE];
		int i = 1;
		i = setupSlider(i);
		i = setupNumber(i);
		i = setupCheckbox(i);
		var[i++] = null;	//最後を null にして末尾を明示
	}

	/**
	 * duration を hour, minute で指定するためにオーバーライド
	 */
	protected int setupNumber(int i){
		rate = i;
		var[i++] = new IntVariable("rate", "rate(frames/sec): ", 1);
		duration = i;
		var[i++] = new HCIntVariable1("duration(hour): ", "duration(minute): ", "duration(h,m)", 600);
		binDuration = i;
		var[i++] = new HCIntVariable2("bDuration", "bin duration(minute): ", 60);
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
		LightON = i;
		var[i++] = new IntVariable("Light.ON", "Light ON (h): ", 0);
		LightOFF = i;
		var[i++] = new IntVariable("Light.OFF", "Light OFF (h): ", 0);
		reduceTimes = i;
		var[i++] = new ThresholdIntVariable("reduce.noise.times", "reduce noise (times): ", 1);
		dilateTimes = i;
		var[i++] = new ThresholdIntVariable("dilate.times", "dilate (times): ", 1);
		return i;
	}
	
	protected int setupSlider(int i){
		dayThreshold = i;
		var[i++] = new IntSliderVariable("day.threshold.min", "threshold day", 0, 0, 255);
		nightThreshold = i;
		var[i++] = new IntSliderVariable("night.threshold.min", "threshold night", 0, 0, 255);
		xorThreshold = i;
		var[i++] = new IntSliderVariable("xor.threshold.min", "threshold min (xor)", 150, 0, 255);
		maxThreshold = i;
		var[i++] = new IntSliderVariable("threshold.max", "threshold max", 255, 0, 255);
		return i;
	}

	protected int setupSubCheckbox(int i){
		subtractBackground = i;
		var[i++] = new ThresholdBooleanVariable("subtract.background", "subtract xor background", false);
		erode = i;
		var[i++] = new ThresholdBooleanVariable("erode", "erode", false);
		return i;
	}

	/**
	 * HC2, HC3 ではトレースを使わない
	 */
	protected int setupCheckbox(int i){
		invertMode = i;
		var[i++] = new ThresholdBooleanVariable("mode.invert", "invert mode", false);
		i = setupSubCheckbox(i);
		return i;
	}
}
