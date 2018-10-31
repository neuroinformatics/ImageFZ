package behavior.setup.parameter;

import behavior.setup.parameter.variable.BooleanVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.util.rmconstants.RMConstants;

public class RMParameter extends Parameter{
	public static int delay;
	public static int delayAfter4;
	public static int NSense;

	protected RMParameter(){}

	protected int setupNumber(int i){
		rate = i;
		var[i++] = new IntVariable("rate", "rate(frames/sec): ", 1);
		duration = i;
		var[i++] = new IntVariable("duration", "duration(sec): ", 1500);

		//binは使用しない
		//binDuration = i;
		//var[i++] = new IntVariable("bDuration", "bin duration(sec): ", 150);

		minSize = i;
		var[i++] = new IntVariable("subject.size.min", "subject size - min(pixels): ", 50);
		maxSize = i;
		var[i++] = new IntVariable("subject.size.max", "subject size - max(pixels): ", 999999);
		frameWidth = i;
		var[i++] = new IntVariable("frame.width", "frame size - width(cm): ", 100);
		frameHeight = i;
		var[i++] = new IntVariable("frame.height", "frame size - height(cm): ", 100);

		//マウスが中央に戻ってドアが閉まってから開くまで
		delay = i;
		var[i++] = new IntVariable("delay.std", "delay　- duration(sec): ", 5);

		if(!RMConstants.isReferenceMemoryMode()){
		    //マウスが餌を4つ食べて中央に戻った後にドアが閉まってから開くまで
		    delayAfter4 = i;
		    var[i++] = new IntVariable("delay.after", "delayAfter4 - duration(sec)", 5);
		}

		return i;
	}

	protected int setupSubCheckbox(int i){//IntakeとOmissionを区別するかどうか
	    NSense = i;
		if(!RMConstants.isOffline())
		    var[i++] = new BooleanVariable("sense", "N-sense mode", false);

		return i;
	}

	protected int setupSubNumber(int i){return i;}
}