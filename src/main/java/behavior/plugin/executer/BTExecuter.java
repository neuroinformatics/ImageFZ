package behavior.plugin.executer;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import behavior.gui.roi.RoiOperator;
import behavior.gui.BehaviorDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.BTAnalyzer;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.controller.InputController;

/**
 * Beam-Test(Online)用のExecuter。棒の上に乗せられたマウスの行動を解析する。
 * 他の実験と異なる特徴は次の四つ。
 * 　　1.Cage数を1で固定している。
 * 　　2.binを使用しない。
 * 　　3.出力数が一定ではないデータがいくつかある。
 * 　　4.実験の終了条件が四種類ある。
 * 　　　　　　（制限時間、棒から落ちる、GoalAreaに到達、Alt+Shiftで実験を中断した場合）
 * 
 * @author Butoh
 * @version Last Modified 091214
 */
public class BTExecuter extends OnlineExecuter {
	//cage数を1で固定する
	private final int CAGE = 1;
	private static InputController input;
	protected ImagePlus[] subtractImp;
	private String[] respectiveFileNames;
	private String[] respectiveHeaderNames;
	protected ScheduledFuture<?> future;

	/**
	 * BTExecuterを構築します。
	 * binを使用しないところで特異的です。
	 */
	// binごとに分割しない
	public BTExecuter(){
		program = Program.BT;
		//今のところCage数は1で固定、なぜならSlipの値を一度に複数のCage分とるのは無理（なはず）だから
		//他にもいろいろ面倒なので1で。
		this.allCage = CAGE;
		
		String[] fileName = {"Distance Per Movement","Duration Per Movement","Speed Per Movement", "Sliped Time"};
		respectiveFileNames = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			respectiveFileNames[i] = fileName[i];

		String[] headerName = {"Phase", "Phase","Phase","Slip"};
		respectiveHeaderNames = new String[headerName.length];
		for(int i = 0; i < headerName.length; i++)
			respectiveHeaderNames[i] = headerName[i];
	}

	/**
	 * BT固有のセットアップを行います。
	 * @param backIp -バックグラウンド画像
	 */
	@Override
	protected void subSetup(ImageProcessor[] backIp){
		//InputControllerのインスタンスを取得
		if(input != null)
			input.close();
		input = InputController.getInstance(InputController.PORT_IO);

		//GoalTimeの取得のためにMainAreaのRectangleを渡す必要がある
		Roi[] fieldRoi = ((RoiOperator) roiOperator).getRoi();
		Rectangle[] fieldRec = new Rectangle[1];
		fieldRec[0] = fieldRoi[0].getBounds();

		//cage数は1で固定されている
		analyze[0] = new BTAnalyzer(backIp[0]);
		((BTAnalyzer) analyze[0]).createField(fieldRec[0]);
	}

	/******
	 * 結果の保存をする。
	 *******/
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());
		boolean writeVersion = (trialNum == 1);

		int activeCage = 1;	//結果を保存するケージ数。途中で interrupt されたケージの結果は保存されない。
		boolean[] endAnalyze = new boolean[allCage];
		Arrays.fill(endAnalyze, true);
		resSaver.setActiveCage(endAnalyze);

		saveImage(backIp);

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];

		resSaver.saveXYResult(0, winOperator.getXYTextPanel(0));
		totalResult[0] = analyze[0].getResults();

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion,setup.isModifiedParameter());

	    winOperator.showOnlineTotalResult(program, activeCage, subjectID, totalResult);

		saveBinResult(writeHeader);
		saveOthers(subjectID, backIp);
	}

	/**
	 * BT固有の画像ウィンドウの設定を行います
	 * @param backIp -バックグラウンド画像
	 */
	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
        subtractImp[0] = new ImagePlus("subtract", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
	}

	/**
	 * BTAnalyzeから現在の画像を取得します。
	 * @param cage -ケージ数
	 */
	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract",((BTAnalyzer) analyze[0]).getSubtractImage());
	}

	@Override
	protected void analyze(String[] subjectID){
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		finishSignal = new CountDownLatch(1);
		Runnable task = new AnalyzeTaskBT();
		final long PERIOD = (long)Math.round(1000 / Parameter.getInt(Parameter.rate));

		readyToStart();
		setStartTimeAndDate(0);

		future = scheduler.scheduleAtFixedRate(task, 0, PERIOD, TimeUnit.MILLISECONDS);

		try{
		    finishSignal.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		scheduler.shutdown();

		//追加
		if(interrupt())
			BehaviorDialog.showMessageDialog("Trial has been interrupted by user.");
	}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){return null;}

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
	    Toolkit.getDefaultToolkit().beep();

	    ((BTAnalyzer)analyze[0]).setStart(true);
	}


	public class AnalyzeTaskBT implements AnalyzeTask{
		private final int allSlice;
		private int cageSlice;
		private boolean endAnalyze;
		private String information;

		public AnalyzeTaskBT(){
            allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;	
			endAnalyze = false;
		}

		@Override
		public void run(){
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
			winOperator.setInfoText(information,0);

			//キーボードで中断した場合
			if(interrupt()) isEnd();

			if(endAnalyze || ((BTAnalyzer)analyze[0]).isGoal()) isEnd();

			cageSlice++;
		}

		public void isEnd(){
			future.cancel(true);
			finishSignal.countDown();
		}
	}

	/**
	 * 結果ファイルを作成します。
	 */
	@Override
	protected void end(){
		final boolean writeDate = true;
		resSaver.writeDate(writeDate,writeDate,respectiveFileNames);
	}

	/**
	 * binごとの解析結果のファイルを作成します。
	 * 但しBTではbinではありません。
	 */
	//歪な流用
	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {BTAnalyzer.DISTANCE_PER_MOVEMENT, BTAnalyzer.DURATION_PER_MOVEMENT, BTAnalyzer.SPEED_PER_MOVEMENT, BTAnalyzer.SLIPED_TIME};
		for(int num = 0; num < option.length; num++){
			List<String> result;
			result = ((BTAnalyzer)analyze[0]).getRespectiveResults(option[num]);
			//要素が0だった場合のNullPo防止
			//実際に必要かは試していない
			if(result.size()==0)
			    result.add("");
			//ヘッダに記入する回数
			final int headerNum = 10;
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(respectiveFileNames[num])).exists());
			resSaver.saveOnlineRepectiveResults(respectiveFileNames[num], respectiveHeaderNames[num], headerNum, result, writeHeader,writeVersion);
		}
	}
}