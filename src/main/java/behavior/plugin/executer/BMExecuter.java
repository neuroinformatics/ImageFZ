package behavior.plugin.executer;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import behavior.controller.InputController;
import behavior.gui.BehaviorDialog;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.gui.roi.RoiOperator;
import behavior.image.ImageCapture;
import behavior.io.BMResultSaver;
import behavior.io.FileManager;
import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.BMAnalyzer;
import behavior.setup.BMSetup;
import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.setup.dialog.BMSetCageDialogPanel;
import behavior.setup.parameter.BMParameter;
import behavior.setup.parameter.Parameter;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

/**
 * 
 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public class BMExecuter extends OnlineExecuter{
	private final int ALLCAGE = 1;
	private final int ALLHOLE = 12;
	private final String sep = System.getProperty("file.separator");
	private static InputController input;
	private ImagePlus[] subtractImp;
	private boolean addTrace = false;//スイッチで終了した場合trueにしてトレースを保存。
	private SwitchWatcher watcher;
	ScheduledFuture<?> future;
	private boolean canGoNext = false;
	ScheduledFuture<?> waitFuture;

	int allSlice;
	int[] cageSlice;
	boolean endFlag;
	boolean[] endAnalyze;
	long startTime;
	int day;
	int frameNum;
	String[] information;
	
	public BMExecuter(){
		this.program = Program.BM;
		allCage = ALLCAGE;
		binFileName = new String[2];
		binFileName[0] = "binFileName[0]";
		binFileName[1] = "binFileName[1]";
	}
	
	protected boolean setup(){
		ONLINE = true;
		calendar = new Calendar[1];
		imageCapture = ImageCapture.getInstance();

		if(imageCapture.setupFailed())
			return true;
		setup = new BMSetup(program, Setup.ONLINE, allCage);//setupをBMSetupに
		if(setup.setup())
			return true;
		//setup.saveSession();
		roiOperator = new RoiOperator(program, allCage);
		//analyze = new Analyzer[allCage];
		analyze = new BMAnalyzer[allCage];
		currentImp = new ImagePlus[allCage];
		traceImp = new ImagePlus[allCage];
		return false;
	}
	
	protected boolean run(int trialNum){
		if(trialNum > 1 && setup.reSetup())
			return true;
		setup.saveSession();
		if(setOtherParameter()) return true;
		if(roiOperator.loadRoi()) return true;
		ImageProcessor[] backIp = getBackground();	//バックグラウンド取得
		resSaver = new BMResultSaver(program, allCage, backIp);
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
			BMAnalyzer.subjectIDCount = 0;
			end();
			return true;
		}
	}
	
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		final boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.ResultsDir) + sep + FileManager.getInstance().getPath(FileManager.SessionID) + "_res.txt").exists());
		final boolean writeVersion = (trialNum == 1);
		boolean writeParameter;
		if(trialNum==1){
			writeParameter = true;
		}else{
			writeParameter = setup.isModifiedParameter();
		}
		int activeCage = 0;	//結果を保存するケージ数。途中で interrupt されたケージの結果は保存されない。
		boolean[] endAnalyze = new boolean[allCage];
		for(int cage = 0; cage < allCage; cage++)
			if(endAnalyze[cage] = (analyze[cage].getState() != Analyzer.NOTHING))
				activeCage++;
		
		saveImage(backIp);

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];
		for(int cage = 0; cage < allCage; cage++){
			resSaver.saveXYResult(cage, winOperator.getXYTextPanel(cage));
			totalResult[cage] = analyze[cage].getResults();
		}
		
		if(ONLINE)
			resSaver.saveOnlineTotalResult(totalResult, calendar, writeHeader, writeVersion, writeParameter);
		else
			resSaver.saveBMOfflineTotalResult(totalResult, writeHeader);
		winOperator.showBMTotalResult(program, activeCage, subjectID, totalResult);

		saveBinResult(writeVersion);
		saveOthers(subjectID, backIp);
		
		String[] result = ((BMAnalyzer) analyze[0]).getProbeResults();
		winOperator.showProbeResult(program, subjectID, result);
		if (new File(FileManager.getInstance().getPath(FileManager.ResultsDir) + sep + FileManager.getInstance().getPath(FileManager.SessionID) + "_probe.txt").exists())   
			((BMResultSaver) resSaver).saveProbeResult(result, false);
		else 
			((BMResultSaver) resSaver).saveProbeResult(result, true);
		
		String result_sel = ((BMAnalyzer) analyze[0]).getSelResult();
		//見にくくなるので_selの結果は表示しない
		((BMResultSaver) resSaver).saveSelResult(result_sel, true);
	}
	
	
	protected void subSetup(ImageProcessor[] backIp){
		addTrace = false;
		if(input != null)
			input.close();
		input = InputController.getInstance(InputController.PORT_IO);
//		Analyzeクラスの初期化（変数名のみ宣言済み）
		for(int cage = 0; cage < allCage; cage++)
			analyze[cage] = new BMAnalyzer(backIp[cage]);
	}
	
	
	
	protected void analyze(String[] subjectID){
		((BMAnalyzer) analyze[0]).setState(false);
		allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
		cageSlice = new int[allCage];	
		Arrays.fill(cageSlice, 0);
		beep = Toolkit.getDefaultToolkit();
		endFlag = false;
		endAnalyze = new boolean[allCage];
		Arrays.fill(endAnalyze, false);
		startTime = System.currentTimeMillis();
		day = 1;
		frameNum = 0;
		information = new String[allCage];
		setStartTimeAndDate(0);
		
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		Runnable task = new AnalyzeTask();
		final long PERIOD = (long)Math.round(1000 / Parameter.getInt(Parameter.rate));
		
		//waitForStart();
		ScheduledExecutorService waitScheduler = Executors.newSingleThreadScheduledExecutor();
		Runnable waitTask = new WaitThread();
		waitFuture = waitScheduler.scheduleAtFixedRate(waitTask, 0, PERIOD, TimeUnit.MILLISECONDS);
		while(!canGoNext);
		watcher = new SwitchWatcher();
		watcher.start();
		
		
		future = scheduler.scheduleAtFixedRate(task, 0, PERIOD, TimeUnit.MILLISECONDS);
		while(!future.isCancelled()){
			try{
				Thread.sleep(100);//スイッチでの終了にすばやく反応するために100とした。（これで問題ない？）
			}catch(InterruptedException e){
				break;
			}
		}
		
	}
	
	private class WaitThread implements Runnable {
		
		private WaitThread() {
			canGoNext = false;
		}

		public void run() {
			while(!input.getInput(0)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			canGoNext = true;
			
			
		}
		
	}
	
	private class AnalyzeTask implements Runnable {

		public void run() {
			ImageProcessor[] currentIp = roiOperator.split((imageCapture.capture()).getProcessor());
			if (((BMAnalyzer) analyze[0]).checkFinished()) {
				addTrace = true;
				setCurrentImage(0, cageSlice[0], currentIp[0]);
				endAnalyze();
				endTask();
				return;
			}
			
			analyze[0].analyzeImage(currentIp[0]);	
			
			if(cageSlice[0] == 0)
				beep.beep();	//各ケージで、実験開始時に beep 音を鳴らす
			analyze[0].calculate(cageSlice[0]);
			winOperator.setXYText(0, analyze[0].getXY(cageSlice[0]));
			setCurrentImage(0, cageSlice[0], currentIp[0]);
			if(analyze[0].binUsed() && cageSlice[0] != 0 && cageSlice[0] % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[0].nextBin();
			if(cageSlice[0] == allSlice)	//allSlice + 1 枚分解析をする
				endAnalyze[0] = true;
			cageSlice[0]++;
			
			
			information[0] = analyze[0].getInfo(subjectID[0], cageSlice[0]);
			winOperator.setInfoText(information[0],0);
			
			if(interrupt())
				endTask();
			if(endAnalyze[0])
				endTask();
		}
		
		public void endTask() {
			future.cancel(true);
		}
		
	}
	
	private class SwitchWatcher extends Thread{
		
		public void run(){
			while(true) {
				try{
				    if(input.getInput(1)) {
					    ((BMAnalyzer) analyze[0]).setState(true);
					    future.cancel(true);
					    break;
				    }
				    Thread.sleep(100);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	
	protected void endAnalyze() {
		endAnalyze[0] = true;
	}
	
	
	public static InputController getLabjackInstance(){
		return input;
	}
	
	protected void setCurrentImage(int cage, int sliceNum, ImageProcessor currentIp){
		currentImp[cage].setProcessor(subjectID[cage], currentIp);
		resSaver.setCurrentImage(cage, currentIp);
		
		ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
		traceImp[cage].setProcessor("trace" + (cage + 1), traceIp);
		if(analyze[cage].binUsed()){
			if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0 || addTrace)
			    resSaver.addTraceImage(cage, traceIp);
		}else{
			if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.duration) * Parameter.getInt(Parameter.rate)) == 0)
				resSaver.addTraceImage(cage, traceIp);
		}
		
		setEachCurrentImage(cage);	//abstract: それぞれの実験の画像表示
	}

	
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
		/*
		ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();
		ip = ip.convertToRGB();
		ip.setColor(Color.red);

		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		for (int i = 1; i <= ALLHOLE; i++) {
			String roiName = path + sep + "hole" + i + ".roi";
			if(!new File(roiName).exists())
				continue;
			try {
				Roi roi = new RoiDecoder(roiName).getRoi();
				roi.drawPixels(ip);
				Rectangle rec = roi.getPolygon().getBounds();
				String str = Integer.toString(i);
				char[] chars = str.toCharArray();
				ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
						rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < ALLCAGE; i++)
			subtractImp[i].setProcessor("subtract", ip);
		*/
	}
	
	
	protected void setEachCurrentImage(int cage){
		ImageProcessor ip = ((BMAnalyzer)analyze[cage]).getSubtractImage();
		ip.setColor(Color.black); //これがないとマウスがいない時にRoiが表示されない。色はblackである必要はない。
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String field_roi_name = path + sep + "Field.roi";
		if (!new File(field_roi_name).exists()) 
			BehaviorDialog.showErrorDialog("Field.roi is not set");
		//for (int i = 1; i <= ALLHOLE; i++) {
		
		//実験中もRoiを表示
		int x, y, width, height, x_center, y_center, field_x, field_y, field_width, field_height;
		
		//int armR = Parameter.getInt(BMParameter.armR);
		//int distOut = Parameter.getInt(BMParameter.distOut);
		int innerR = BMParameter.innerR;
		int outerR = BMParameter.outerR;
		String roiName;
		try {
			Rectangle fieldRec = new RoiDecoder(field_roi_name).getRoi().getBounds();
			field_x = fieldRec.x;
			field_y = fieldRec.y;
			field_width = fieldRec.width;
			field_height = fieldRec.height;
			int diameter = (field_width + field_height) / 2;
			OvalRoi ovalField = new OvalRoi(0, 0, diameter, diameter);
			ovalField.drawPixels(ip);
			for (int i = 1; i <= ALLHOLE; i++) {
				roiName = path + sep + "hole" + i + ".roi";
				if(!new File(roiName).exists())
					continue;
				Roi roi = new RoiDecoder(roiName).getRoi();
				x = roi.getBounds().x - field_x;
				y = roi.getBounds().y - field_y;
				width = roi.getBounds().width;
				height = roi.getBounds().height;
				x_center = x + width/2;
				y_center = y + height/2;
				OvalRoi inner_oval_roi = new OvalRoi(x_center - innerR, y_center - innerR, innerR*2, innerR*2);
				OvalRoi outer_oval_roi = new OvalRoi(x_center - outerR, y_center - outerR, outerR*2, outerR*2);
				inner_oval_roi.drawPixels(ip);
				outer_oval_roi.drawPixels(ip);
			}
			
			String targetRoiName = path + sep + "hole" + BMSetCageDialogPanel.getTarget() + ".roi";
			Roi roi = new RoiDecoder(targetRoiName).getRoi();
			Rectangle rec = roi.getBounds();
			String str = "T";
			char[] chars = str.toCharArray();
			field_x = new RoiDecoder(field_roi_name).getRoi().getBounds().x;
			field_y = new RoiDecoder(field_roi_name).getRoi().getBounds().y;
			ip.drawString(str, rec.x - field_x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2,
					rec.y + (rec.height - field_y + ip.getFontMetrics().getAscent()) / 2 - 5);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ip);
	}
	
	
	protected void saveBinResult(boolean writeVersion){
		;
	}
	
}