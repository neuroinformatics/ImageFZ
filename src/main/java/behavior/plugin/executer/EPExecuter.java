package behavior.plugin.executer;

import java.io.File;
import ij.process.ImageProcessor;
import ij.*;

import behavior.gui.BehaviorDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;

import behavior.setup.Program;
import behavior.setup.parameter.Parameter;

import behavior.plugin.analyzer.EPAnalyzer;

/**
 *   @author anonymous
 * 　　@author　Butoh
 *   @version Last Modified 091214
 */
public class EPExecuter extends OnlineExecuter{
	protected ImagePlus[] subtractImp;

	public EPExecuter(){
		program = Program.EP;
		//cage数は1で良いとのこと。
		allCage = 1;
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
	protected void setEachWindow(ImageProcessor[] backIp) {
		subtractImp = new ImagePlus[allCage];
		subtractImp[0] = new ImagePlus("subtract", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
	}

	@Override
	protected void setEachCurrentImage(int cage) {
		subtractImp[cage].setProcessor("subtract",((EPAnalyzer) analyze[0]).getSubtractImage());
	}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new AnalyzeTaskEP();
	}

	@Override
	protected void readyToStart(){
		BehaviorDialog.showMessageDialog("please click OK to start");
	}


	public class AnalyzeTaskEP  implements AnalyzeTask{
		private final int allSlice;
		private int cageSlice;
		private boolean endAnalyze;
		private String information;

		public AnalyzeTaskEP(){
            allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;	
			endAnalyze = false;
		}

		@Override
		public void run(){
			try{
			final int CAGE = 0;

			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),CAGE);;
			/* 元のforループ部分 */
			analyze[CAGE].analyzeImage(currentIp);
			analyze[CAGE].calculate(cageSlice);

			winOperator.setXYText(CAGE, analyze[CAGE].getXY(cageSlice));
			setCurrentImage(CAGE, cageSlice, currentIp);

			if(cageSlice != 0 && cageSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[CAGE].nextBin();

			if(cageSlice == allSlice){	//allSlice + 1 枚分解析をする
				information = analyze[CAGE].getInfo(subjectID[CAGE], cageSlice+1);//応急処置
				endAnalyze = true;
			}else{
				information = analyze[CAGE].getInfo(subjectID[CAGE], cageSlice);
			}
			winOperator.setInfoText(information,CAGE);

			if((cageSlice-1) != allSlice)
	            timer.run((double)cageSlice/Parameter.getInt(Parameter.rate));
			//Timerで中断した場合
			if(timer.isInterrupt()){
				saveTraceInInterrupt(CAGE, cageSlice);
			    analyze[CAGE].interruptAnalysis();
			    isEnd();
			}
			//キーボードで中断した場合
			if(interrupt()) isEnd();

			if(endAnalyze) isEnd();

			cageSlice++;}catch (Throwable e){e.printStackTrace();}
		}

		public void isEnd(){
			futures[0].cancel(true);
			finishSignal.countDown();
		}
	}

	@Override
	protected void end(){
		final boolean writeDate = true;
		resSaver.writeDate(writeDate,writeDate,binFileName);
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {EPAnalyzer.BIN_DISTANCE, EPAnalyzer.CENTER_TIME, EPAnalyzer.BIN_TIME_OPEN, EPAnalyzer.BIN_TIME_CLOSE,
				EPAnalyzer.BIN_NE_OPEN, EPAnalyzer.BIN_NE_CLOSE};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			result[0] = ((EPAnalyzer)analyze[0]).getBinResult(option[num]);
			resSaver.saveOnlineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}
}