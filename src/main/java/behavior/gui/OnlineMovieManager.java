package behavior.gui;

import javax.swing.JCheckBox;
import javax.swing.JSlider;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import behavior.image.ImageCapture;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.HCParameter;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.Program;

/**
 * ThresholdDialogPanel のスライダーの値に合わせて動画を表示する(ONLINE)。
 * 
 * プログラム毎に動作が違うのだが、なかなか綺麗に記述することができない。
 * 良い方法があれば教えてほしい。
 */
public class OnlineMovieManager extends MovieManager{

	private boolean isRunning;
	private JSlider[] sliders;
	private ExtendedJTextField[] fields;
	private JCheckBox[] checks;

	private ImagePlus[] live;
	private ImagePlus[] subtract;
	private ImagePlus[] xor;
	private ImagePlus[] day;
	private ImagePlus[] night;
	private ImageProcessor[] backIp;
	private ImageProcessor[] prevIp;
	private WindowOperator winOperator;

	private boolean xorFlag;
	private boolean HCFlag;
	private Program program;

	public OnlineMovieManager(JSlider[] sliders, ExtendedJTextField[] fields, JCheckBox[] checks, 
			Program program, ImageProcessor backIp){
		isRunning = true;
		this.sliders = sliders;
		this.fields = fields;
		this.checks = checks;
		this.program = program;

		xorFlag = (Parameter.xorThreshold != 0); // XOR が必要かどうか
		HCFlag = program.isHC(); // HC かどうか

		live = new ImagePlus[1];
		live[0] = new ImagePlus("Live Movie", backIp.duplicate());
		if(HCFlag){
			day = new ImagePlus[1];
			day[0] = new ImagePlus("day", backIp.duplicate());
			night = new ImagePlus[1];
			night[0] = new ImagePlus("night", backIp.duplicate());
		} else {
			subtract = new ImagePlus[1];
			subtract[0] = new ImagePlus("subtract", backIp.duplicate());
		}

		if(xorFlag){
			xor = new ImagePlus[1];
			xor[0] = new ImagePlus("XOR", backIp.duplicate());
		}

		this.backIp = new ImageProcessor[1];
		this.backIp[0] = backIp.duplicate();
		prevIp = new ImageProcessor[1];
		prevIp[0] = backIp.duplicate();

		winOperator = WindowOperator.getInstance(1, this.backIp);
		winOperator.setImageWindow(live, WindowOperator.LEFT_UP);
		if(HCFlag){
			winOperator.setImageWindow(day, WindowOperator.RIGHT_DOWN);
			winOperator.setImageWindow(night, WindowOperator.LEFT_DOWN);
		} else if(xorFlag)
			winOperator.setImageWindow(subtract, WindowOperator.RIGHT_DOWN);
		else
			winOperator.setImageWindow(subtract, WindowOperator.RIGHT_UP);

		if(xorFlag)
			winOperator.setImageWindow(xor, WindowOperator.RIGHT_UP);

	}

	public void run(){
		while(isRunning){
			ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();

			boolean invert = false;
			if(Parameter.invertMode != 0)
				invert = (checks[Parameter.invertMode].getSelectedObjects() != null);

			int dilate = 1;
			if(HCFlag){
				try{
					dilate = Integer.parseInt(fields[HCParameter.dilateTimes].getText());
				} catch(NumberFormatException e){
					dilate = 1;
				}
			}

			int reduce = 1;
			if(HCFlag){
				try{
					reduce = Integer.parseInt(fields[HCParameter.reduceTimes].getText());
				} catch(NumberFormatException e){
					reduce = 1;
				}
			}

			live[0].setProcessor("Live Movie", ip);
			if(HCFlag){
				ImageProcessor dayIp = getSubtractImage(ip, backIp[0],
						sliders[Parameter.dayThreshold].getValue(),
						sliders[Parameter.maxThreshold].getValue(),
						invert);
				ImageProcessor nightIp = getSubtractImage(ip, backIp[0],
						sliders[Parameter.nightThreshold].getValue(),
						sliders[Parameter.maxThreshold].getValue(),
						invert);
				for(int i = 0; i < reduce; i++){
					dayIp.medianFilter();
					nightIp.medianFilter();
				}
				for(int i = 0; i < dilate; i++){
					dayIp.dilate();
					nightIp.dilate();
				}
				day[0].setProcessor("day", dayIp);
				night[0].setProcessor("night", nightIp);
			} else {
				ImageProcessor subIp = getSubtractImage(ip, backIp[0],
						sliders[Parameter.minThreshold].getValue(),
						sliders[Parameter.maxThreshold].getValue(),
						invert);
				if(program != Program.TS && program != Program.FZ && program != Program.SI)  // TS,FZ,OSIはdilateなしのよう。
					subIp.dilate();
				subtract[0].setProcessor("subtract", subIp);
			}
			if(xorFlag){
				ImageProcessor xorIp, sub;
				if(checks[Parameter.subtractBackground].getSelectedObjects() != null){
					if(HCFlag)
						sub = day[0].getProcessor();
					else
						sub = subtract[0].getProcessor();
					xorIp = getXORImage(prevIp[0], sub,
							sliders[Parameter.xorThreshold].getValue(),
							sliders[Parameter.maxThreshold].getValue());
					prevIp[0] = sub;
				} else {
					xorIp = getXORImage(prevIp[0], ip,
							sliders[Parameter.xorThreshold].getValue(),
							sliders[Parameter.maxThreshold].getValue());
					prevIp[0] = ip;
				}

				for(int i = 0; i < reduce; i++)
					xorIp.medianFilter();

				if(checks[Parameter.erode].getSelectedObjects() != null)
					xorIp.erode();	

				xor[0].setProcessor("XOR", xorIp);
			}
			try{
				sleep(1000 / ((IntVariable)Parameter.getInstance().getVar(Parameter.rate)).getVariable());
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public void end(){
		winOperator.closeWindows();
		isRunning = false;
	}
}
