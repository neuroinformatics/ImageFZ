package behavior.setup.parameter;

import behavior.setup.parameter.variable.IntSliderVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.TMStringVariable;

public class TMParameter extends Parameter{
	public static int testType;

	protected TMParameter(){}

	protected int setupSlider(int i){
		minThreshold = i;
		var[i++] = new IntSliderVariable("MinThreshold", "threshold min", 0, 0, 255);
		maxThreshold = i;
		var[i++] = new IntSliderVariable("MaxThreshold", "threshold max", 255, 0, 255);
		return i;
	}

	protected int setupNumber(int i){
		rate = i;
		var[i++] = new IntVariable("Rate", "rate(frames/sec): ", 2);
		duration = i;
		var[i++] = new IntVariable("Duration", "duration(sec): ", 600);
		minSize = i;
		var[i++] = new IntVariable("MinSize", "subject size - min(pixels): ", 50);
		maxSize = i;
		var[i++] = new IntVariable("MaxSize", "subject size - max(pixels): ", 999999);
		frameWidth = i;
		var[i++] = new IntVariable("Width", "frame size - width(cm): ", 55);
		frameHeight = i;
		var[i++] = new IntVariable("Height", "frame size - height(cm): ", 55);
		testType = i;
		var[i++] = new TMStringVariable("testType","Test Type:","LL" );
		
		return i;
	}

	@Override
	protected int setupSubNumber(int i){return i;}

	@Override
	protected int setupSubCheckbox(int i){return i;}
}