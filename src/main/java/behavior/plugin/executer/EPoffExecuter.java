package behavior.plugin.executer;

import java.io.File;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import behavior.setup.Program;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.EPAnalyzer;

public class EPoffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp;

	public EPoffExecuter(){
		program = Program.EP;
		String[] fileName = {"dist", "ctime","Spent Time(Open)","Spent Time(Close)", "NE(Open)", "NE(Close)"};
		binFileName = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			binFileName[i] = fileName[i];
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		analyze[0] = new EPAnalyzer(backIp[0]);
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {EPAnalyzer.BIN_DISTANCE, EPAnalyzer.CENTER_TIME, EPAnalyzer.BIN_TIME_OPEN, EPAnalyzer.BIN_TIME_CLOSE,
				EPAnalyzer.BIN_NE_OPEN, EPAnalyzer.BIN_NE_CLOSE};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			result[0] = ((EPAnalyzer)analyze[0]).getBinResult(option[num]);
			resSaver.saveOfflineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}

	@Override
	protected void setEachCurrentImage(int cage) {
		subtractImp[cage].setProcessor("subtract1", ((EPAnalyzer)analyze[cage]).getSubtractImage());
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[1];
		subtractImp[0] = new ImagePlus("subtract1", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}
}