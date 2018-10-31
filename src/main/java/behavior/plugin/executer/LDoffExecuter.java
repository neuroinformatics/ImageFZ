package behavior.plugin.executer;

import java.io.File;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.LDAnalyzer;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.setup.Program;

public class LDoffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp;
//	private OutputControler output = OutputControler.getInstance();

	public LDoffExecuter(){
		program = Program.LD;
		String[] fileName = {"distD", "distL", "timeD", "timeL", "trans"};
		binFileName = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			binFileName[i] = fileName[i];
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		for(int cage = 0; cage < allCage; cage++)
			analyze[cage] = new LDAnalyzer(backIp[cage]);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ((LDAnalyzer)analyze[cage]).getSubtractImage());
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {LDAnalyzer.DISTANCE_DARK, LDAnalyzer.DISTANCE_LIGHT, LDAnalyzer.TIME_DARK,
				LDAnalyzer.TIME_LIGHT, LDAnalyzer.TRANSITION};

		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++)
				result[cage] = ((LDAnalyzer)analyze[cage]).getBinResult(option[num]);
			resSaver.saveOfflineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}

}