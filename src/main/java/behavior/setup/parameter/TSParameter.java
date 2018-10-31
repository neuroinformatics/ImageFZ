package behavior.setup.parameter;

import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntSliderVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.ThresholdBooleanVariable;

public class TSParameter extends Parameter{
	public static int freezCriterion;
	public static int minFreezDuration;

	protected TSParameter(){}

	protected int setupSlider(int i){
		minThreshold = i;
		var[i++] = new IntSliderVariable("threshold.min", "threshold min", 0, 0, 255);
		xorThreshold = i;
		var[i++] = new IntSliderVariable("xor.threshold.min", "threshold min (xor)", 150, 0, 255);
		maxThreshold = i;
		var[i++] = new IntSliderVariable("threshold.max", "threshold max", 255, 0, 255);
		return i;
	}

	protected int setupSubNumber(int i){
		freezCriterion = i;
		var[i++] = new IntVariable("freezing.criterion", "immobile criterion(pixel): ", 20);
		minFreezDuration = i;
		var[i++] = new DoubleVariable("freez.duration.min", "minimum immobile duration -(sec): ", 2.0);
		return i;
	}

	protected int setupSubCheckbox(int i){
		subtractBackground = i;
		var[i++] = new ThresholdBooleanVariable("subtract.background", "subtract xor background", false);
		erode = i;
		var[i++] = new ThresholdBooleanVariable("erode", "erode", false);
		return i;
	}
}