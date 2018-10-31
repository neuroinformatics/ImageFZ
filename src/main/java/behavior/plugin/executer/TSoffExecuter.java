package behavior.plugin.executer;

import java.io.File;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.TSAnalyzer;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.setup.Program;

/**
 * TailSuspention‚Ì‚½‚ß‚ÌExecuter(Offline)
 * @author Butoh
 * @version Last Modified 091202
 */
public class TSoffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp, xorImp;

	public TSoffExecuter(){
		program = Program.TS;
		binFileName = new String[3];
		binFileName[0] = "dist";
		binFileName[1] = "immobile";
		binFileName[2] = "area";
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		analyze[0] = new TSAnalyzer(backIp[0]);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		subtractImp[0] = new ImagePlus("subtract" + 1, backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
		xorImp = new ImagePlus[allCage];
		xorImp[0] = new ImagePlus("xor" + 1, backIp[0]);
		winOperator.setImageWindow(xorImp, WindowOperator.RIGHT_UP);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ((TSAnalyzer)analyze[cage]).getSubtractImage());
		xorImp[cage].setProcessor("xor" + (cage + 1), ((TSAnalyzer)analyze[cage]).getXorImage());
	}

	@Override
	protected void save(String subjectID,int trialNum, int cageNum,boolean showResult){
		boolean writeVersion = (trialNum == 1);

		resSaver.saveOfflineTraceImage();
	
		resSaver.saveOfflineXYResult(0, ((TSAnalyzer)analyze[0]).getXYText());

		saveBinResult(writeVersion);
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {TSAnalyzer.BIN_DISTANCE, TSAnalyzer.BIN_FREEZ_PERCENT, TSAnalyzer.BIN_XOR_AREA};

		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++)
				result[cage] = ((TSAnalyzer)analyze[cage]).getBinResult(option[num]);
			resSaver.saveOfflineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}
}