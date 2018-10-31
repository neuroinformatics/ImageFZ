package behavior.plugin.executer;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import behavior.controller.InputController;
import behavior.gui.WindowOperator;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.plugin.analyzer.YMAnalyzer;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;

/**
 * Y-MazeのためのExecuter(Online)
 * @author Butoh
 * @version Last Modified 091214
 */
public class YMExecuter extends OnlineExecuter{
	protected ImagePlus[] subtractImp;
	private InputController input;
	private final boolean writeDate = true;
	private PushListener listener;
	protected ScheduledFuture<?> future;
	protected CountDownLatch finishSignal;

	public YMExecuter(){
	    program = Program.YM;
	    //cage数は1で良いとのこと。
	    allCage = 1;

	    String[] fileName = {"dist"};
	    binFileName = new String[fileName.length];
	    for(int i = 0; i < fileName.length; i++)
		    binFileName[i] = fileName[i];
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		if(input != null)
			input.close();
		input = InputController.getInstance(InputController.PORT_IO);

		analyze[0] = new YMAnalyzer(backIp[0]);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp) {
		subtractImp = new ImagePlus[allCage];
		subtractImp[0] = new ImagePlus("subtract", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
	}

	@Override
	protected void setEachCurrentImage(int cage) {
		subtractImp[cage].setProcessor("subtract",((YMAnalyzer) analyze[0]).getSubtractImage());
	}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new AnalyzeTaskYM();
	}

	@Override
	protected void readyToStart(){
		for(;;){
			if(input.getInput(0))
				break;
			try{
			    Thread.sleep(100);
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
		}

	    listener = new PushListener();
	    listener.start();
	}

	public class AnalyzeTaskYM extends DefaultAnalyzeTask implements AnalyzeTask,Runnable{
		public AnalyzeTaskYM() {
			allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;
			endAnalyze = false;
		}

		@Override
		public void run(){
			try{
			final int CAGE = 0;

			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),CAGE);
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
			if(timer.isInterrupt() || listener.isInterrupt() || interrupt()){
				saveTraceInInterrupt(CAGE, cageSlice);
			    analyze[CAGE].interruptAnalysis();
			    isEnd();
			}

			if(endAnalyze) isEnd();

			cageSlice++;}catch(Exception e){
				e.printStackTrace();}
		}
	}
	
	protected void save(final int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());
		boolean writeVersion = (trialNum == 1);
		boolean writeParameter;
		if(trialNum==1){
			writeParameter = true;
		}else{
			writeParameter = setup.isModifiedParameter();
		}

		int activeCage = 1;

		boolean[] endAnalyze = new boolean[allCage];
		Arrays.fill(endAnalyze, true);
		resSaver.setActiveCage(endAnalyze);

		saveImage(backIp);

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];

		resSaver.saveXYResult(0, winOperator.getXYTextPanel(0));
		totalResult[0] = analyze[0].getResults();

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion, writeParameter);
	    winOperator.showOnlineTotalResult(program, activeCage, subjectID, totalResult);

		saveBinResult(writeVersion);
		saveOtherResults();
	}

	@Override
	protected void end(){
		resSaver.writeDate(writeDate,writeDate,binFileName);

		String path = FileManager.getInstance().getPath(FileManager.ResultsDir)+File.separator+FileManager.getInstance().getPath(FileManager.SessionID);
		FileCreate create = new FileCreate();
		create.writeDate(path+"-AltState.txt");
		create.writeDate(path+"-EnterTime.txt");
		create.writeDate(path+"-ExitTime.txt");
		create.writeDate(path+"-StayTime.txt");
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {YMAnalyzer.BIN_DISTANCE};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			result[0] = ((YMAnalyzer)analyze[0]).getBinResult(option[num]);
			resSaver.saveOnlineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}

	private void saveOtherResults(){
		String[] subjectID = FileManager.getInstance().getPaths(FileManager.SubjectID);
		String path = FileManager.getInstance().getPath(FileManager.ResultsDir)+File.separator+FileManager.getInstance().getPath(FileManager.SessionID);

		String resultPath = path+"-AltState.txt";
		FileCreate creater = new FileCreate(resultPath);
		creater.write(subjectID[0]+((YMAnalyzer)analyze[0]).getOtherResult(YMAnalyzer.FileType.ALTERNATION_STATE), true);

		String resultPath2 = path+"-EnterTime.txt";
		FileCreate creater2 = new FileCreate(resultPath2);
		creater2.write(subjectID[0]+((YMAnalyzer)analyze[0]).getOtherResult(YMAnalyzer.FileType.ENTER_TIME), true);

		String resultPath3 = path+"-ExitTime.txt";
		FileCreate creater3 = new FileCreate(resultPath3);
		creater3.write(subjectID[0]+((YMAnalyzer)analyze[0]).getOtherResult(YMAnalyzer.FileType.EXIT_TIME), true);

		String resultPath4 = path+"-StayTime.txt";
		FileCreate creater4 = new FileCreate(resultPath4);
		creater4.write(subjectID[0]+((YMAnalyzer)analyze[0]).getOtherResult(YMAnalyzer.FileType.STAY_TIME), true);
	}

	/**
	 * 2番ボタンを押したとき解析を中断するためのスレッド
	 * @author Butoh
	 */
	private class PushListener extends Thread{
		private boolean isInterrupt = false;

		public void run(){
			while(true){
				try{
				    if(input.getInput(1)){
					    isInterrupt = true;
					    break;
				    }
				    Thread.sleep(100);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		public boolean isInterrupt(){
			return isInterrupt;
		}
	}
}