package behavior.plugin.executer;


import java.io.File;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.OFAnalyzer;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.setup.Program;

public class OFoffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp;

	public OFoffExecuter(){
		program = Program.OF;
		binFileName = new String[3];
		binFileName[0] = "dist";
		binFileName[1] = "ctime";
		binFileName[2] = "area";
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		for(int cage = 0; cage < allCage; cage++)
			analyze[cage] = new OFAnalyzer(backIp[cage]);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus(cage + " subtract", backIp[cage]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ((OFAnalyzer)analyze[cage]).getSubtractImage());
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {OFAnalyzer.BIN_DISTANCE, OFAnalyzer.CENTER_TIME, OFAnalyzer.PARTITION_AREA};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++)
				result[cage] = ((OFAnalyzer)analyze[cage]).getBinResult(option[num]);
			resSaver.saveOfflineBinResult(binFileName[num], result, writeHeader, true, writeVersion);
		}
	}
}