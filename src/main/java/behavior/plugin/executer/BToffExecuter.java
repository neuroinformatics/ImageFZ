package behavior.plugin.executer;

import java.io.File;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.BTAnalyzer;
import behavior.setup.Program;

/**
 * Beam-Test(Offline)用のExecuter。棒の上に乗せられたマウスの行動を解析する。
 * Onlineと異なる特徴は次の三つ。
 * 　　1.Labjackを使用しない
 * 　　2.Slip,SlipedTime,Fall,Goalは出力しない
 * 　　3.実験の終了条件は区別しない
 * 
 * @author Butoh
 */
public class BToffExecuter extends OfflineExecuter {
	private ImagePlus[] subtractImp;
	private String[] respectiveFileNames;
	private String[] respectiveHeaderNames;
	
	public BToffExecuter() {
		program = Program.BTO;

		//OfflineではSlipを削る
		String[] fileName = {"Distance Per Movement","Duration Per Movement","Speed Per Movement", };
		respectiveFileNames = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			respectiveFileNames[i] = fileName[i];

		String[] headerName = {"Phase", "Phase","Phase"};
		respectiveHeaderNames = new String[headerName.length];
		for(int i = 0; i < headerName.length; i++)
			respectiveHeaderNames[i] = headerName[i];
	}

	protected void subSetup(ImageProcessor[] backIp) {
		analyze[0] = new BTAnalyzer(backIp[0]);
	}

	protected void setEachWindow(ImageProcessor[] backIp) {
		subtractImp = new ImagePlus[allCage];
        subtractImp[0] = new ImagePlus("subtract" + 1, backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
	}

	protected void setEachCurrentImage(int cage) {
		subtractImp[cage].setProcessor("subtract" + 1,((BTAnalyzer) analyze[0]).getSubtractImage());
	}

	@Override
	//BeamTestではLabjackをOfflineでは使用しないのでOnlineとOfflineの
	//calcurateが異なっている
	protected void analyze(ImageStack allImage){
		allSlice = allImage.getSize();
		((BTAnalyzer)analyze[0]).setFlag(program, allSlice);
		analyze[0].resetDuration(allSlice - 2);
		for(int slice = 1; slice < allSlice; slice++){
			ImageProcessor currentIp = allImage.getProcessor(slice);
			analyze[0].analyzeImage(currentIp);
            //if(analyze.mouseExists()){  //マウスが認識されれば、解析と結果表示
			//ここを修正
			((BTAnalyzer)analyze[0]).calculate(slice - 1);
			//if(analyze[0].binUsed() && (slice - 1) != 0 && (slice - 1) % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
			//	analyze[0].nextBin();
			winOperator.setXYText(0, analyze[0].getXY(slice - 1));
			setCurrentImage(slice - 1, currentIp);
            //}
		}
		addRestResult(allSlice - 2);
	}

    //BeamTestではLabjackをOfflineでは使用しないのでOnlineとOfflineの
	//getResultが異なっている
	@Override
	protected void save(String subjectID,int trialNum, int cageNum,boolean showResult){
		boolean writeHeader = !(new File(FileManager.getInstance().getSavePath(FileManager.totalResPath)).exists());
		boolean writeVersion = (trialNum == 1);
		boolean writeParameter;
		if(cageNum == 0){
			writeParameter = setup.isModifiedParameter();
		}else{
			writeParameter = false;
		}

		resSaver.saveOfflineTraceImage();

		resSaver.saveOfflineXYResult(0, winOperator.getXYTextPanel(0));

		String[] totalResult;
		totalResult = analyze[0].getResults();

		resSaver.saveOfflineTotalResult(totalResult, writeHeader, writeVersion,  writeParameter);

		String[] resultsLine = new String[totalResult.length+1];
		resultsLine[0] = subjectID;
		for(int i=0;i<totalResult.length;i++){
			resultsLine[i+1] = totalResult[i];
		}
		results.add(resultsLine);

		if(showResult){
		    winOperator.showOfflineTotalResult(program,results);
		    results.clear();
		}

		saveBinResult(writeVersion);
	}

	//SlipedTimeは記録しない
	@Override
	protected void saveBinResult(boolean writeVersion) {
		int[] option = {BTAnalyzer.DISTANCE_PER_MOVEMENT, BTAnalyzer.DURATION_PER_MOVEMENT, BTAnalyzer.SPEED_PER_MOVEMENT};
		final int headerNum = 10;
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(respectiveFileNames[num])).exists());
			List<String> result;
			result = ((BTAnalyzer)analyze[0]).getRespectiveResults(option[num]);
			result.add("");
			resSaver.saveOfflineRepectiveResults(respectiveFileNames[num], respectiveHeaderNames[num], headerNum, result, writeHeader,writeVersion);
		}
	}

	//やっぱりtraceは使う
	/*
	public boolean traceUsed(){
		return false;
	}*/
}
