package behavior.plugin.executer;

import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.AnalyzerForStart;
import behavior.plugin.analyzer.OFAnalyzer;
import behavior.gui.BehaviorDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.util.Timer;


public class OFExecuter extends OnlineExecuter {
	private ImagePlus[] subtractImp;
	protected AnalyzerForStart[] startAnalyzer;

	public OFExecuter(){
		this(4);
	}

	public OFExecuter(int allCage){
		program = Program.OF;
		this.allCage = allCage;
		String[] fileName = {"dist", "ctime", "area"};
		binFileName = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			binFileName[i] = fileName[i];
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		startAnalyzer = new AnalyzerForStart[allCage];
		for(int cage = 0; cage < allCage; cage++){
			analyze[cage] = new OFAnalyzer(backIp[cage]);
			startAnalyzer[cage] = new AnalyzerForStart(backIp[cage], program);
		}
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP,existMice);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ((OFAnalyzer)analyze[cage]).getSubtractImage());
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

        //追加、OFSelectCage()を組み込んだ
        boolean[] isStart = new boolean[allCage];
        Arrays.fill(isStart, false);

        if(allCage >= 4){ 
            for(int cage=0;cage<allCage;cage++){
		        if(!existMice[cage]){
			        finishSignal.countDown();
			        analyze[cage].interruptAnalysis();
			        //start扱いにしておく
			        isStart[cage] = true;
		        }			
            }
        }
 
        //追加、startAnalyze(cage)を組み込んだ
        boolean isEnd;
        while(true){
        	ImageProcessor[] currentIp = roiOperator.split((imageCapture.capture()).getProcessor());
		    for(int cage=0;cage<allCage;cage++){
		    	if(!isStart[cage]){
			        if(startAnalyzer[cage].startAnalyze(currentIp[cage])){
			        	setStartTimeAndDate(cage);
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

		//追加
		if(interrupt() || timer.isInterrupt())
			   BehaviorDialog.showMessageDialog("Trial has been interrupted by user.");
		timer.finalize();
	}

	@Override
	protected void readyToStart(){}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new AnalyzeTaskOF(cage);
	}

	public class AnalyzeTaskOF implements AnalyzeTask{
		protected int cage = 0;
		protected int allSlice;
		protected int cageSlice;
		protected boolean endAnalyze;
		protected String information;

		public AnalyzeTaskOF(int cageNO){
			allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;
			endAnalyze = false;
			this.cage=cageNO;
		}

		@Override
		public void run(){
			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),cage);

			analyze[cage].analyzeImage(currentIp);
			analyze[cage].calculate(cageSlice);

			winOperator.setXYText(cage, analyze[cage].getXY(cageSlice));
			setCurrentImage(cage, cageSlice, currentIp);

			if(analyze[cage].binUsed() && cageSlice != 0 && cageSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[cage].nextBin();

			if(cageSlice == allSlice){	//allSlice + 1 枚分解析をする
				information = analyze[cage].getInfo(subjectID[cage], cageSlice+1);//応急処置
				endAnalyze = true;
			}else{
				information = analyze[cage].getInfo(subjectID[cage], cageSlice);
			}
			winOperator.setInfoText(information,cage);

			if(allCage==1 && (cageSlice-1) != allSlice){
	            timer.run((double)cageSlice/Parameter.getInt(Parameter.rate));
			}

			//追加
			if(analyze[cage].getState() == Analyzer.INTERRUPT){
				saveTraceInInterrupt(cage, cageSlice);
			    analyze[cage].interruptAnalysis();
			    isEnd();
			}

			//追加
			if(interrupt())
				isEnd();

			//追加
			if (timer!=null && timer.isInterrupt()) {
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

			cageSlice++;
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
		
		resSaver.setActiveCage(existMice);

		saveImage(backIp);

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];
		for(int cage = 0; cage < allCage; cage++){
			if(!existMice[cage]) continue;
	        resSaver.saveXYResult(cage, winOperator.getXYTextPanel(cage));
    		totalResult[cage] = analyze[cage].getResults();
		}

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion, writeParameter);
		winOperator.showOnlineTotalResult(program, allCage, existMice, subjectID, totalResult);

		saveBinResult(writeHeader);
		saveOthers(subjectID, backIp);
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {OFAnalyzer.BIN_DISTANCE, OFAnalyzer.CENTER_TIME, OFAnalyzer.PARTITION_AREA};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++)
				result[cage] = ((OFAnalyzer)analyze[cage]).getBinResult(option[num]);
			resSaver.saveOnlineBinResult(binFileName[num], result, writeHeader, true, writeVersion);
		}
	}
}