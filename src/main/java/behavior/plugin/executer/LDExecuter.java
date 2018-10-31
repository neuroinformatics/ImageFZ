package behavior.plugin.executer;

import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.LDAnalyzer;
import behavior.controller.OutputController;
import behavior.gui.BehaviorDialog;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.gui.roi.LDRoiOperator;
import behavior.setup.parameter.Parameter;
import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.util.Timer;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.io.ResultSaver;

/**
 * 
 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public class LDExecuter extends OnlineExecuter{
	private ImagePlus[] subtractImp;
	private OutputController output;

	public LDExecuter(int allCage){
		program = Program.LD;
		this.allCage = allCage;
		String[] fileName = {"distD", "distL", "timeD", "timeL", "trans"};
		binFileName = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			binFileName[i] = fileName[i];
	}

	@Override
	protected boolean setup(){
		ONLINE = true;
		imageCapture = ImageCapture.getInstance();
		
		if(imageCapture.setupFailed())
			return true;
		setup = new Setup(program, Setup.ONLINE, allCage);
		if(setup.setup())
			return true;
		setup.saveSession();
		//ここを変更
		roiOperator = new LDRoiOperator(allCage);

		analyze = new Analyzer[allCage];
		currentImp = new ImagePlus[allCage];
		traceImp = new ImagePlus[allCage];
		calendar = new Calendar[allCage];
		return false;
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		for(int cage = 0; cage < allCage; cage++){
			analyze[cage] = new LDAnalyzer(backIp[cage]);
		}
		
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
		    winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP,existMice);
	}

	/**バックグラウンドを取得する**/
	@Override
	protected ImageProcessor[] getBackground(){
		IJ.wait(3000);
		ImageProcessor[] backIp = roiOperator.split(setup.getBackIp());
		return backIp;
	}

	@Override
	protected boolean run(int trialNum){
		try{
		if(trialNum > 1 && setup.reSetup())
			return true;
		setup.saveSession();
		if(roiOperator.loadRoi()) return true;
		
		//completeを押した後にドアを閉める処理
		output = OutputController.getInstance();
		output.setup(OutputController.LD_TYPE);
		output.clear(OutputController.ALL_CHANNEL);
		
		ImageProcessor[] backIp = getBackground();	//バックグラウンド取得
		resSaver = new ResultSaver(program, allCage, backIp);
		subjectID = setup.getSubjectID();
		resSaver.setSubjectID(subjectID);

		existMice = setup.getExistMice();

		setWindow(backIp);  //表示するウィンドウの設定

		subSetup(backIp);  //abstract: 各実験固有のウィンドウ表示やそのほかの設定を行う
		analyze(subjectID);	//解析
		save(trialNum, subjectID, backIp);	//保存
		
		NextAnalysisDialog next = new NextAnalysisDialog();
		while(next.isVisible()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		winOperator.closeWindows();
		if(next.nextAnalysis())
			return false;
		else{
			end();
			return true;
		}}catch(Throwable e){
			e.printStackTrace();
			return true;
		}
	}

	@Override
	protected void analyze(String[] subjectID){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(allCage);
		finishSignal = new CountDownLatch(allCage);
		Runnable[] task = new Runnable[allCage];
		futures = new ScheduledFuture<?>[allCage];
		for(int cage=0;cage<allCage;cage++){
			task[cage] = createAnalyzeTask(cage);
		}

		final long PERIOD = (long)Math.round(1000 / Parameter.getInt(Parameter.rate));

        boolean[] isStart = new boolean[allCage];
        Arrays.fill(isStart, false);

        for(int cage=0;cage<allCage;cage++){
	        if(!existMice[cage]){
		        finishSignal.countDown();
		        analyze[cage].interruptAnalysis();
		        //start扱いにしておく
		        isStart[cage] = true;
	        }			
        }
 
        //追加、startAnalyze(cage)を組み込んだ
        boolean isEnd;
        while(true){
        	ImageProcessor[] currentIp = roiOperator.split((imageCapture.capture()).getProcessor());
		    for(int cage=0;cage<allCage;cage++){
		    	if(!isStart[cage]){
		    		analyze[cage].analyzeImage(currentIp[cage]);
			        if(((LDAnalyzer)analyze[cage]).startAnalyze(0)){
			        	setStartTimeAndDate(cage);
			        	output.controlOutput(OutputController.CHANNEL[cage]);
		                futures[cage] = scheduler.scheduleAtFixedRate(task[cage], 0, PERIOD, TimeUnit.MILLISECONDS);
		                Toolkit.getDefaultToolkit().beep();
		                isStart[cage] = true;
			        }
		    	}
	        }

		    //全部のchamberが開始したとき
		    isEnd = true;
			for(int i=0;i<isStart.length;i++){
				isEnd &= isStart[i];
			}
			if(isEnd){
				timer = new Timer(allCage);
				break;
			}

			try{
		        TimeUnit.MILLISECONDS.sleep(100L);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}

		try{
		    finishSignal.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		scheduler.shutdown();

		if(interrupt() || timer.isInterrupt())
			   BehaviorDialog.showMessageDialog("Trial has been interrupted by user.");
		timer.finalize();
	}

	@Override
	protected void readyToStart(){}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new AnalyzeTaskLD(cage);
	}

	public class AnalyzeTaskLD implements AnalyzeTask{
		protected int cage = 0;
		protected int allSlice;
		protected int cageSlice;
		protected boolean endAnalyze;
		protected String information;

		public AnalyzeTaskLD(int cageNO){
			allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;
			endAnalyze = false;
			this.cage=cageNO;
		}

		@Override
		public void run(){
			try{
			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),cage);
			//追加
			

			analyze[cage].analyzeImage(currentIp);
			analyze[cage].calculate(cageSlice);

			winOperator.setXYText(cage, analyze[cage].getXY(cageSlice));
			setCurrentImage(cage, cageSlice, currentIp);

			if(analyze[cage].binUsed() && cageSlice != 0 && cageSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[cage].nextBin();

			if(cageSlice == allSlice){	//allSlice + 1 枚分解析をする
				information = analyze[cage].getInfo(subjectID[cage], cageSlice+1);
				endAnalyze = true;
			}else{
				information = analyze[cage].getInfo(subjectID[cage], cageSlice);
			}
			winOperator.setInfoText(information,cage);

			if(allCage==1 && (cageSlice-1) != allSlice){
	            timer.run((double)cageSlice/Parameter.getInt(Parameter.rate));
			}

			//追加
			if(interrupt())
				isEnd();
			
			if (timer != null && timer.isInterrupt()) {
				for (int cage = 0; cage < allCage; cage++) {
					analyze[cage].interruptAnalysis();
				}
				for (int cage = 0; cage < allCage; cage++) {
					if(analyze[cage].getState() == Analyzer.INTERRUPT)
						saveTraceInInterrupt(cage, cageSlice);
				}
				isEnd();
			}

			if(endAnalyze) isEnd();

			cageSlice++;}catch(Throwable e){
				e.printStackTrace();
			}
		}

		public void isEnd(){
			futures[cage].cancel(true);
			finishSignal.countDown();
		}
	}

	@Override
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());	//最初の解析のときは、結果フォルダにヘッダを記載する
		boolean writeVersion = (trialNum==1);
		boolean writeParameter;
		if(trialNum==1){
			writeParameter = true;
		}else{
			writeParameter = setup.isModifiedParameter();
		}

		boolean[] activeCage = new boolean[allCage];
		for(int cage = 0; cage < allCage; cage++)
			activeCage[cage] = (analyze[cage].getState() != Analyzer.NOTHING);
		resSaver.setActiveCage(activeCage);

		saveImage(backIp);
		
		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];
		for(int cage = 0; cage < allCage; cage++){
			if(!existMice[cage]) continue;
			resSaver.saveXYResult(cage, winOperator.getXYTextPanel(cage));
			totalResult[cage] = analyze[cage].getResults();
		}

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader,writeVersion,writeParameter);
		winOperator.showOnlineTotalResult(program, allCage, activeCage, subjectID, totalResult);

		saveBinResult(writeVersion);
		saveOthers(subjectID, backIp);
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
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++)
				result[cage] = ((LDAnalyzer)analyze[cage]).getBinResult(option[num]);
			resSaver.saveOnlineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}

	@Override
	protected void end(){
		resSaver.writeDate(binFileName);
		IJ.showMessage("Please click OK to close the doors");
		output.clear(OutputController.ALL_CHANNEL);
		output.close();
	}
}