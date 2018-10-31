package behavior.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.io.TiffDecoder;
import ij.process.ImageProcessor;

import javax.swing.JCheckBox;
import javax.swing.JSlider;

import behavior.io.FileManager;
import behavior.io.ImageLoader;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.variable.IntVariable;

/**
 * ThresholdDialogPanel のスライダーの値に合わせて動画を表示する(OFFLINE)。
 */
public class OfflineMovieManager extends MovieManager{
	private boolean isRunning;

	private JSlider[] sliders;
	private JCheckBox[] checks;

	private String subjectID;
	private ImageStack stack;
	private ImageProcessor[] backIp;
	private WindowOperator winOperator;
	private ImagePlus[] live;
	private ImagePlus[] subtract;
	private ImagePlus[] xor;
	private int nextSlice;
	private boolean xorFlag;
	private Program program;

	public OfflineMovieManager(JSlider[] sliders, ExtendedJTextField[] fields, JCheckBox[] checks,
			Program program, String subjectID){
		this.sliders = sliders;
		this.checks = checks;
		this.program = program;

		xorFlag = (Parameter.xorThreshold != 0);

		backIp = new ImageProcessor[1];
		live = new ImagePlus[1];
		subtract = new ImagePlus[1];
		if(xorFlag)
			xor = new ImagePlus[1];

		this.subjectID = subjectID;
		if(program == Program.TM){
			StringBuilder str = new StringBuilder(subjectID);
			for(int i=0;i<2;i++){
				str.setCharAt(str.lastIndexOf("-"),'_');
			}
		    String imageID = str.toString();

			FileManager fileManager = FileManager.getInstance();
			String extension = ".tiff";
			if(new File(fileManager.getPath(FileManager.ImagesDir) +File.separator+ fileManager.getPath(FileManager.SessionID)+File.separator+imageID + ".tif").exists())
				extension = ".tif";

			TiffDecoder imgTd = new TiffDecoder(fileManager.getPath(FileManager.ImagesDir) + File.separator+ fileManager.getPath(FileManager.SessionID)+File.separator, imageID + extension);
			Opener open = new Opener();
			ImagePlus stackImp = null;
			try{
				stackImp = open.openTiffStack(imgTd.getTiffInfo());
			}catch(FileNotFoundException e){
				IJ.error("no file:" +imageID + extension + "(in ImageLoader)");
			}catch(IOException e){
				IJ.error("Input error:" + imageID + extension + "(in ImageLoader)");
			}
			stack = stackImp.getStack();	
		}else{
		    stack = (new ImageLoader()).loadImage(subjectID);
		}
		backIp[0] = stack.getProcessor(stack.getSize());
		live[0] = new ImagePlus(subjectID, backIp[0]);
		subtract[0] = new ImagePlus(subjectID + " subtract", backIp[0]);
		if(xorFlag)
			xor[0] = new ImagePlus(subjectID + " XOR", backIp[0]);
		nextSlice = 1;

		winOperator = WindowOperator.getInstance(1, backIp);
		winOperator.setImageWindow(live, WindowOperator.LEFT_UP);
		if(xorFlag){
			winOperator.setImageWindow(subtract, WindowOperator.RIGHT_DOWN);
			winOperator.setImageWindow(xor, WindowOperator.RIGHT_UP);
		} else
			winOperator.setImageWindow(subtract, WindowOperator.RIGHT_UP);
	}

	public void run(){
		isRunning = true;

		while(isRunning){
			boolean invert = false;
			if(Parameter.invertMode != 0)

				invert = (checks[Parameter.invertMode].getSelectedObjects() != null);

			if(nextSlice >= stack.getSize())
				nextSlice = 1;

			live[0].setProcessor(subjectID, stack.getProcessor(nextSlice));
			ImageProcessor ip = stack.getProcessor(nextSlice);

			ImageProcessor subIp = getSubtractImage(ip, backIp[0],
					sliders[Parameter.minThreshold].getValue(),
					sliders[Parameter.maxThreshold].getValue(),
					invert);
			if(program != Program.TS && program != Program.FZ && program != Program.SI)  // TS,FZ,OSIはdilateなしのよう。
				subIp.dilate();
			subtract[0].setProcessor(subjectID + " subtract", subIp);
			if(xorFlag){
				ImageProcessor prevIp, xorIp, sub;

				if(checks[Parameter.subtractBackground].getSelectedObjects() != null){
					sub = subtract[0].getProcessor();
					if(nextSlice == 1)
						prevIp = getSubtractImage(stack.getProcessor(stack.getSize() - 1), backIp[0],
								sliders[Parameter.minThreshold].getValue(),
								sliders[Parameter.maxThreshold].getValue(),
								invert);
					else
						prevIp = getSubtractImage(stack.getProcessor(nextSlice - 1), backIp[0],
								sliders[Parameter.minThreshold].getValue(),
								sliders[Parameter.maxThreshold].getValue(),
								invert);
					xorIp = getXORImage(prevIp, sub,
							sliders[Parameter.xorThreshold].getValue(),
							sliders[Parameter.maxThreshold].getValue());
				} else {
					if(nextSlice == 1)
						prevIp = stack.getProcessor(stack.getSize() - 1);
					else
						prevIp = stack.getProcessor(nextSlice - 1);
					xorIp = getXORImage(prevIp, ip,
							sliders[Parameter.xorThreshold].getValue(),
							sliders[Parameter.maxThreshold].getValue());
				}

				xorIp.medianFilter();

				if(checks[Parameter.erode].getSelectedObjects() != null)
					xorIp.erode();

				xor[0].setProcessor(subjectID + " XOR", xorIp);
			}				
			nextSlice++;

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
