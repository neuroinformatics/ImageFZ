package behavior.setup.parameter;

import behavior.setup.Program;
import behavior.setup.parameter.variable.BooleanVariable;
import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntSliderVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.TMStringVariable;
import behavior.setup.parameter.variable.ThresholdBooleanVariable;
import behavior.setup.parameter.variable.Variable;

public abstract class Parameter{
	static boolean endSetting = false;

	private static Parameter parameter;

	public static int minThreshold;
	public static int maxThreshold;
	public static int rate;
	public static int duration;
	public static int binDuration;
	public static int minSize;
	public static int maxSize;
	public static int frameWidth;
	public static int frameHeight;
	public static int invertMode;

	// ThresholdDialogPanel で使用する、全プログラム共通ではないパラメータもここで宣言しておく。
	public static int xorThreshold=0;
	public static int dayThreshold;
	public static int nightThreshold;
	public static int erode;
	public static int subtractBackground;

	protected final int MAX_VARIABLE = 35;	//パラメータの数、多めにとっておけばよい
	protected static Variable[] var;

	protected Parameter(){
		var = new Variable[MAX_VARIABLE];
		int i = 1;
		i = setupSlider(i);
		i = setupNumber(i);
		i = setupCheckbox(i);
		var[i++] = null;	//最後を null にして末尾を明示
	}

	public static void initialize(Program program){
		switch(program){
		case LD:    parameter = new LDParameter(); break;
		case OF:    parameter = new OFParameter(); break;
		case FZS:   parameter = new FZShockParameter(); break;
		case FZ:    parameter = new FZParameter(); break;
		case TS:    parameter = new TSParameter(); break;
		case RM:    parameter = new RMParameter(); break;
		case YM:    parameter = new YMParameter(); break;
		case EP:    parameter = new EPParameter(); break;
		case BT:
		case BTO:   parameter = new BTParameter(); break;
		case HC1:   parameter = new HCParameter();  break;
		case HC2:   parameter = new HCParameter(); break;
		case HC3:   parameter = new HCParameter(); break;
		case CSI:   parameter = new CSIParameter();break;
		case OLDCSI:parameter = new OldCSIParameter();break;
		case OFC:   parameter = new OFCParameter();break;
		//case WM:  parameter = new WMParameter(); break;
		//case WMP: parameter = new WMParameter(); break;
		case BM:    parameter = new BMParameter(); break;
		case SI:    parameter = new SIParameter(); break;
		case TM:    parameter = new TMParameter(); break;
		default: throw new IllegalArgumentException("selected program has no parameter");
		}
	}

	public synchronized static Parameter getInstance(){
		return parameter;
	}

	public static void endSetting(){
		endSetting = true;
	}

	protected int setupSlider(int i){
		minThreshold = i;
		var[i++] = new IntSliderVariable("threshold.min", "threshold min", 0, 0, 255);
		maxThreshold = i;
		var[i++] = new IntSliderVariable("threshold.max", "threshold max", 255, 0, 255);
		return i;
	}

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

	protected abstract int setupSubNumber(int i); 

	protected int setupCheckbox(int i){
		invertMode = i;
		var[i++] = new ThresholdBooleanVariable("mode.invert", "invert mode", false);
		i = setupSubCheckbox(i);
		return i;
	}

	protected abstract int setupSubCheckbox(int i); 

	//以下、ParamManager との通信用
	public Variable[] getVar(){
		return var;
	}

	public Variable getVar(int type){
		if(type >= var.length || type == 0)
			throw new IllegalArgumentException("no such type:" + type);
		return var[type];
	}

	/*パラメータを取り出すメソッド、取り出す型によってメソッドを変える*/
	public static int getInt(int type){
		if(!endSetting)
			throw new IllegalStateException("パラメータを設定前に呼び出しています");
		if(type >= var.length || type == 0)
			throw new IllegalArgumentException("no such type:" + type);
		int data = 0;
		try{
			data = ((IntVariable)var[type]).getVariable();
		}catch(ClassCastException e){
			throw new IllegalArgumentException(var[type].getName() + " is not int");
		}
		return data;
	}

	public static boolean getBoolean(int type){
		if(!endSetting)
			throw new IllegalStateException("パラメータを設定前に呼び出しています");
		if(type >= var.length || type == 0)
			throw new IllegalArgumentException("no such type:" + type);
		boolean data = false;
		try{
			data = ((BooleanVariable)var[type]).getVariable();
		}catch(ClassCastException e){
			throw new IllegalArgumentException(var[type].getName() + " is not boolean");
		}
		return data;
	}

	public static double getDouble(int type){
		if(!endSetting)
			throw new IllegalStateException("パラメータを設定前に呼び出しています");
		if(type >= var.length || type == 0)
			throw new IllegalArgumentException("no such type:" + type);
		double data = 0;
		try{
			data = ((DoubleVariable)var[type]).getVariable();
		}catch(ClassCastException e){
			throw new IllegalArgumentException(var[type].getName() + " is not double");
		}
		return data;
	}

	public static String getString(int type){
		if(!endSetting)
			throw new IllegalStateException("パラメータを設定前に呼び出しています");
		if(type >= var.length || type == 0)
			throw new IllegalArgumentException("no such type:" + type);
		String data = "";
		try{
			data = ((TMStringVariable)var[type]).getVariable();
		}catch(ClassCastException e){
			throw new IllegalArgumentException(var[type].getName() + " is not String");
		}
		return data;
	}
	public void setVar(Variable[] var){
		Parameter.var = var;
	}
}