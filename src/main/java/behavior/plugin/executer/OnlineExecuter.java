package behavior.plugin.executer;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.io.File;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import behavior.gui.BehaviorDialog;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.gui.roi.RoiOperator;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.io.ResultSaver;
import behavior.plugin.analyzer.Analyzer;
import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.setup.parameter.Parameter;
import behavior.util.Timer;
/************************************************************
 *　このクラスは、全てのオンライン（リアルタイムでのマウス画像取得）実験プログラムの基礎となるもので、
 *　各々のオンライン実験プログラムは、このクラスを継承し、abstract メソッドを埋め込むことで作られます。
 *　このクラスで行うのは、全ての実験で共通な、ユーザーからのプロジェクト名等入力、ウィンドウの表示、
 *　リアルタイム画像取得、結果の保存などです。
 ***************************************************************/
/**
 * 
 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public abstract class OnlineExecuter extends ProgramExecuter{
	protected Setup setup;
	protected RoiOperator roiOperator;
	protected WindowOperator winOperator;
	protected ResultSaver resSaver;
	protected Analyzer[] analyze;

	protected Program program;
	protected int allCage; //サブクラスで設定されるべきものです
	protected String[] subjectID, binFileName;
	protected ImagePlus currentImp[];
	protected ImagePlus traceImp[];
	protected void initialize(){};
	protected Toolkit beep;
	protected String resultLine = "";
	protected ScheduledFuture<?>[] futures;
	protected CountDownLatch finishSignal;
	protected boolean[] existMice;

	protected Timer timer = null;
	protected Calendar[] calendar;

	/*****
	 * プロジェクトID、セッションID、パラメータについてユーザからの入力を受けて、
	 * メンバ変数の初期化を行なう
	 *******/
	protected boolean setup(){
		ONLINE = true;
		imageCapture = ImageCapture.getInstance();

		if(imageCapture.setupFailed())
			return true;
		setup = new Setup(program, Setup.ONLINE, allCage);
		if(setup.setup())
			return true;
		setup.saveSession();
		roiOperator = new RoiOperator(program, allCage);
		analyze = new Analyzer[allCage];
		currentImp = new ImagePlus[allCage];
		traceImp = new ImagePlus[allCage];
		calendar = new Calendar[allCage];
		return false;
	}

	/***
	 *　ユーザーが実感する、設定から解析までのプロセス。
	 *　次の解析を問うダイアローグで yes を押すと、この部分が繰り返される
	 ***/
	protected boolean run(int trialNum){
		try{
		if(trialNum > 1 && setup.reSetup())
			return true;
		setup.saveSession();
		if(setOtherParameter()) return true;
		if(roiOperator.loadRoi()) return true;
		IJ.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		ImageProcessor[] backIp = getBackground();	//バックグラウンド取得
		resSaver = new ResultSaver(program, allCage, backIp);
		subjectID = setup.getSubjectID();
		resSaver.setSubjectID(subjectID);

		existMice = setup.getExistMice();

		setWindow(backIp);  //表示するウィンドウの設定

		subSetup(backIp);  //abstract: 各実験固有のウィンドウ表示やそのほかの設定を行う
		analyze(subjectID);	//解析
		save(trialNum, subjectID, backIp);	//保存
		}catch (Throwable e){e.printStackTrace();}
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
		}
	}

	/**
	 * 他に特に設定する必要があるものはこれに記述する。
	 * @return trueを返すと終了。
	 */
	protected boolean setOtherParameter(){
		return false;
	}

	/**
	 * 各実験固有のウィンドウ表示、その他
	 */
	protected abstract void subSetup(ImageProcessor[] backIp);

	/**
	 * 実験終了時の動作。
	 * デフォルトではbinごとの結果ファイルに日付を書き込む。
	 */
	protected void end(){
		resSaver.writeDate(binFileName);
	}

	/**バックグラウンドを取得する**/
	protected ImageProcessor[] getBackground(){
		ImageProcessor[] backIp = roiOperator.split(setup.getBackIp());
		return backIp;
	}

	/******
	 * 表示するウィンドウの設定。表示するのは、各ケージの画像とそれを処理したもの（各実験により異なる）と、
	 * 全体の情報を表示する info ウィンドウ、各マウスのXY座標を表示する XY ウィンドウである。
	 *******/
	protected void setWindow(ImageProcessor[] backIp){
		winOperator = WindowOperator.getInstance(allCage, backIp);
		for(int cage = 0; cage < allCage; cage++){
		    currentImp[cage] = new ImagePlus(subjectID[cage], backIp[cage]);
		}
		winOperator.setImageWindow(currentImp, WindowOperator.LEFT_UP,existMice);
		setEachWindow(backIp);	//abstract: 各実験固有のウィンドウを表示する

		for(int cage = 0; cage < allCage; cage++){
			traceImp[cage] = new ImagePlus("trace" + (cage + 1), backIp[cage]);
		}
		winOperator.setImageWindow(traceImp, WindowOperator.LEFT_DOWN,existMice);
		resSaver.setTraceImage(backIp);

		winOperator.setInfoWindow(program);
		for(int i=0;i<existMice.length;i++)
			if(!existMice[i]) winOperator.setInfoText("***Empty***", i);
		winOperator.setXYWindow(program,existMice);
	}

	/**
	 * 各実験固有のウィンドウの設定。
	 */
	protected abstract void setEachWindow(ImageProcessor[] backIp);

	/********
	 *　解析中の一連の動作をするメソッド。一般的には、画像を取得し、Analyze に渡して実際の解析をして、
	 *　結果を受け取って表示させる、という動作を繰り返す。
	 *********/
	protected void analyze(String[] subjectID){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(allCage);
		finishSignal = new CountDownLatch(allCage);
		Runnable[] task = new Runnable[allCage];
		futures = new ScheduledFuture<?>[allCage];
		for(int cage=0;cage<allCage;cage++){
			task[cage] = createAnalyzeTask(cage);
		}

		//FrameRate=3などにすると端数分ずれる
		final long PERIOD = (long)Math.round(1000000 / Parameter.getInt(Parameter.rate));

		readyToStart();

        timer = new Timer(allCage);

		for(int cage=0;cage<allCage;cage++){
			setStartTimeAndDate(cage);
		    futures[cage] = scheduler.scheduleAtFixedRate(task[cage], 0, PERIOD, TimeUnit.MICROSECONDS);
		    Toolkit.getDefaultToolkit().beep();
	    }

		try{
		    finishSignal.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		scheduler.shutdown();

		if(interrupt() || timer.isInterrupt())
			   BehaviorDialog.showMessageDialog("Trial has been interrupted by user. time : " + timer.getEndTimebySec());
		timer.finalize();
	}

	protected void readyToStart(){}

	protected void setStartTimeAndDate(int cage){
		calendar[cage] = Calendar.getInstance();
		calendar[cage].set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
		calendar[cage].set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		calendar[cage].set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		calendar[cage].set(Calendar.AM_PM, Calendar.getInstance().get(Calendar.AM_PM));
		calendar[cage].set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR));
		calendar[cage].set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
		calendar[cage].set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));
	}

	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new DefaultAnalyzeTask(cage);
	}

	protected interface AnalyzeTask extends Runnable{
		public void run();
		public void isEnd();
	}

	public class DefaultAnalyzeTask implements AnalyzeTask,Runnable{
		protected int cage = 0;
		protected int allSlice;
		protected int cageSlice;
		protected boolean endAnalyze;
		protected String information;

		public DefaultAnalyzeTask(){}

		public DefaultAnalyzeTask(int cageNO){
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

			if(analyze[cage].binUsed() && cageSlice != 0 && cageSlice%(Parameter.getInt(Parameter.binDuration)*Parameter.getInt(Parameter.rate)) == 0)
				analyze[cage].nextBin();

			if(cageSlice == allSlice){	//allSlice + 1 枚分解析をする
				information = analyze[cage].getInfo(subjectID[cage], cageSlice+1);//応急処置
				endAnalyze = true;
			}else{
				information = analyze[cage].getInfo(subjectID[cage], cageSlice);
			}
			winOperator.setInfoText(information,cage);

			if((cageSlice-1) != allSlice){
	            timer.run((double)cageSlice/Parameter.getInt(Parameter.rate));
			}
			//Timerで中断した場合
			if((timer != null && timer.isInterrupt()) || interrupt()){
				saveTraceInInterrupt(cage, cageSlice);
			    analyze[cage].interruptAnalysis();
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

	/**
	 * 実験中にshift + altを押すと中断する。
	 * @return shift + altが押されたかどうか。
	 */
	protected boolean interrupt(){
		if(IJ.shiftKeyDown() && IJ.altKeyDown()){
			IJ.showMessage("the current analysis was interrupted.(shift + alt key was pressed)");
			for(int cage = 0; cage < allCage; cage++)
				analyze[cage].interruptAnalysis();
			return true;
		}
		return false;
	}

	/**
	 * interrupt(timer)で終了した場合にtraceを保存する。
	 * RM,EP参照。
	 */
	protected synchronized void saveTraceInInterrupt(int cage, int sliceNum){
		//setCurrentImage()でtraceを保存するのと被らないようにするため。
		if(analyze[cage].binUsed()){
		    if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
			    return;
		}else{
			if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.duration) * Parameter.getInt(Parameter.rate)) == 0)
				return;
		}

		ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
		resSaver.addTraceImage(cage, traceIp);
	}

	/******
	 *現在の画像を表示したり保存したりする
	 *******/
	protected synchronized void setCurrentImage(int cage, int sliceNum, ImageProcessor currentIp){
		currentImp[cage].setProcessor(subjectID[cage], currentIp);
		resSaver.setCurrentImage(cage, currentIp);

		ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
		traceImp[cage].setProcessor("trace" + (cage + 1), traceIp);
		if(analyze[cage].binUsed()){
			if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
			    resSaver.addTraceImage(cage, traceIp);
		}else{
	        if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.duration) * Parameter.getInt(Parameter.rate)) == 0){
				resSaver.addTraceImage(cage, traceIp);
			}
		}

		setEachCurrentImage(cage);	//abstract: それぞれの実験の画像表示
	}

	/**
	 * 各実験固有の画像の表示、保存。
	 */
	protected abstract void setEachCurrentImage(int cage);

	/******
	 * 結果の保存をする。
	 *******/
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());	//最初の解析のときは、結果フォルダにヘッダを記載する
		boolean writeVersion = (trialNum==1);
		boolean writeParameter;
		if(trialNum==1){
			writeParameter = true;
		}else{
			writeParameter = setup.isModifiedParameter();
		}

		boolean[] endAnalyze = new boolean[allCage];
		for(int cage=0; cage<allCage; cage++){
			endAnalyze[cage] = (existMice[cage] && (analyze[cage].getState() != Analyzer.NOTHING));
	    }
		resSaver.setActiveCage(endAnalyze);

		saveImage(backIp);

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];
		for(int cage=0; cage<allCage; cage++){
			if(!existMice[cage]) continue;
			resSaver.saveXYResult(cage, winOperator.getXYTextPanel(cage));
			totalResult[cage] = analyze[cage].getResults();
		}

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion, writeParameter);
	    winOperator.showOnlineTotalResult(program, allCage, endAnalyze, subjectID, totalResult);

		saveBinResult(writeVersion);
		saveOthers(subjectID, backIp);
	}

	/**
	 * 画像の保存。
	 */
	protected void saveImage(ImageProcessor[] backIp){
		resSaver.saveImage(backIp);
	}

	/**
	 * binごとの結果を保存する。
	 */
	protected abstract void saveBinResult(boolean writeVerion);

	/**
	 * その他保存するものがある場合はここに記述する。
	 */
	protected void saveOthers(String[] subjectID, ImageProcessor[] backIp){}
}