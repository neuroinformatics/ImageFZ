package behavior.plugin.executer;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import behavior.controller.OutputController;
import behavior.gui.BehaviorDialog;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.io.FZResultSaver;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.FZParameter;
import behavior.util.Timer;
import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.FZAnalyzer;

public class FZExecuter extends OnlineExecuter{
	private OutputController output = OutputController.getInstance();
	protected ImagePlus[] subtractImp, xorImp;
	private ImageStack[] xorImageStack;
	private ArrayList<ShockUnit> shockUnits = new ArrayList<ShockUnit>();
	private int LCM;

	public FZExecuter(final int allCage){
		program = Program.FZ;
		this.allCage = allCage;
		String[] fileName = {"dist", "immobile"};
		binFileName = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			binFileName[i] = fileName[i];
	}

	@Override
	protected boolean run(int trialNum){
		if(trialNum > 1 && setup.reSetup())
			return true;

		if(roiOperator.loadRoi()) return true;
		IJ.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		ImageProcessor[] backIp = getBackground();

		//変更、刺激中の画像を別に記録するため
		resSaver = new FZResultSaver(program, allCage, backIp);

		subjectID = setup.getSubjectID();
		resSaver.setSubjectID(subjectID);

		existMice = setup.getExistMice();

        try{
        	loadReference();
		    saveSession();
	    }catch(FileNotFoundException e){
		    IJ.showMessage(String.valueOf(e));
		    return true;
	    }catch(IOException e){
		    IJ.showMessage(String.valueOf(e));
		    return true;
	    }

		setWindow(backIp);

		subSetup(backIp);
		analyze(subjectID);
		save(trialNum, subjectID, backIp);
		
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

	private void saveSession() throws FileNotFoundException,IOException{
		subjectID = setup.getSubjectID();
		final String path = FileManager.getInstance().getPath(FileManager.sessionPath);
		if(setup.isFirstTrial())
			(new FileCreate()).createNewFile(path);

		String line;
		StringBuilder allLine = new StringBuilder(200);
		if(!FileManager.getInstance().getReferenceFileName().equals("#NoUse")){
		    BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.referencePath)));
		    while((line = reader.readLine()) != null){
			    if(line.startsWith("#")) continue;
			    String[] token = line.split("\t");
			    if(token.length==3 && token[0].equals("s")){
				    allLine.append(token[2]+" ");
				}
			}
		    reader.close();
		}

		for(int cage=0; cage<allCage; cage++){
			if(existMice[cage]){
			    (new FileCreate()).write(path, subjectID[cage] +"\t"+allLine.toString().trim(), true);
			}
		}
	}

	private void loadReference() throws FileNotFoundException,IOException{
		if(FileManager.getInstance().getReferenceFileName().equals("#NoUse"))
			return;
		/*
		 * あらかじめ作成されたReferenceファイルから、実験中に出力する刺激についての設定を取得する。
		 * ファイルには[刺激の種類<タブ>開始時間<タブ>継続期間]という形式で一行ずつ記録されている。
		 * なお、時間指定は整数値であることが期待される。
		 */
		String line;
		StringBuilder allLine = new StringBuilder(200);
		try{
			BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.referencePath)));
			while((line = reader.readLine()) != null){
				allLine.append(line + "\t");
			}
			reader.close();
		}catch(FileNotFoundException e){
			IJ.showMessage(String.valueOf(e));
		}catch(IOException e){
			IJ.showMessage(String.valueOf(e));
		}
		StringTokenizer referToken = new StringTokenizer(allLine.toString(),"\t");

		shockUnits.clear();
		while(referToken.hasMoreTokens()){
			try{
			    shockUnits.add(new ShockUnit(referToken.nextToken(),Double.parseDouble(referToken.nextToken()),
					Double.parseDouble(referToken.nextToken())));
			}catch(ClassCastException e){/*ignore*/}
		}

		Collections.sort(shockUnits); //開始時間が早い順に並び替え
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		for(int cage=0; cage<allCage; cage++){
			analyze[cage] = new FZAnalyzer(backIp[cage]);
		}

		xorImageStack = new ImageStack[allCage];
		for(int cage=0; cage<allCage; cage++){
			if(existMice[cage]){
			    xorImageStack[cage] = (new ImagePlus("xor", backIp[cage])).createEmptyStack();
			}
		}

		if(FileManager.getInstance().getReferenceFileName().equals("#NoUse"))
			return;

		//出力（刺激装置）の初期化
		if(output.setup())
			throw new IllegalStateException();
		output.clear(OutputController.ALL_CHANNEL);
	}

	@Override
	protected void analyze(String[] subjectID){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		finishSignal = new CountDownLatch(1);
		Runnable[] task = new Runnable[1];
		futures = new ScheduledFuture<?>[1];
		task[0] = createAnalyzeTask(0);

		final int rate = Parameter.getInt(Parameter.rate);
		final int shockRate = Parameter.getInt(FZParameter.shockCaptureRate);

        //互除法によりGCDを計算してからLCMを算出する
		int m = Math.max(rate, shockRate);
		int n = Math.min(rate, shockRate);
		int temp;
		while(m%n!=0){
			temp = n;
			n = m%n;
			m = temp; 
		}
		LCM = rate*shockRate/n;

		final long PERIOD = (long)Math.round(1000000/LCM);

		readyToStart();

		timer = new Timer(allCage);

		setStartTimeAndDate(0);
	    futures[0] = scheduler.scheduleAtFixedRate(task[0], 0, PERIOD, TimeUnit.MICROSECONDS);
	    Toolkit.getDefaultToolkit().beep();

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
	protected void setStartTimeAndDate(int cage){
		calendar[0] = Calendar.getInstance();
		calendar[0].set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
		calendar[0].set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		calendar[0].set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		calendar[0].set(Calendar.AM_PM, Calendar.getInstance().get(Calendar.AM_PM));
		calendar[0].set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR));
		calendar[0].set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
		calendar[0].set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));
		for(int i=1;i<allCage;i++){
			calendar[i] = calendar[i-1];
		}
	}
	@Override
	protected void readyToStart(){
		BehaviorDialog.showMessageDialog("please click OK to start");
	}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new AnalyzeTaskFZ();
	}

	public class AnalyzeTaskFZ implements AnalyzeTask,Runnable{
		protected int allSlice;
		protected int cageSlice;
		protected boolean endAnalyze;
		protected String information;
		private boolean isShocking;
		private int shockNO;
		private ShockUnit shock;
		private double stopT;
		private double stopS;
		private boolean isT;
		private boolean isS;
		private int shockRate;
		private int rate;

		public AnalyzeTaskFZ(){
			allSlice = Parameter.getInt(FZParameter.shockCaptureRate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;
			endAnalyze = false;
			isShocking = false;
			stopT=-1;
			stopS=-1;
			isT=false;
			isS=false;

			shockNO = 0;

			if(shockUnits.size()!=0){
			    shock = shockUnits.get(shockNO++);
			}

			rate = Parameter.getInt(Parameter.rate);
			shockRate = Parameter.getInt(FZParameter.shockCaptureRate);
		}

		@Override
		public void run(){
			try{
		    if((cageSlice%LCM)%(LCM/rate)==0){
		    	if(shockUnits.size()!=0){
		    	    checkShock(cageSlice);
		    	}

		    	ImageProcessor currentIp = imageCapture.capture().getProcessor();

		    	for(int cage=0;cage<allCage;cage++){
		    		if(existMice[cage]){
		    	        ImageProcessor cageIp = roiOperator.split(currentIp,cage);

			            analyze[cage].analyzeImage(cageIp);
			            analyze[cage].calculate(cageSlice);
			
			            setCurrentImage(cage, cageSlice, cageIp);
			            if(isShocking && (cageSlice%LCM)%(LCM/shockRate)==0){
			    	        ((FZResultSaver)resSaver).setShockImage(cage, cageIp);
			            }

			            winOperator.setXYText(cage, analyze[cage].getXY(cageSlice));

			            if(cageSlice != 0 && cageSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(FZParameter.shockCaptureRate)) == 0){
				            analyze[cage].nextBin();
		    	        }

			            if(cageSlice == allSlice){	//allSlice + 1 枚分解析をする
			                information = analyze[cage].getInfo(subjectID[cage], cageSlice+1);//応急処置
			                endAnalyze = true;
			            }else{
			                information = analyze[cage].getInfo(subjectID[cage], cageSlice);
			            }
			            winOperator.setInfoText(information,cage);
		    		}
		    	}
		    }else if(isShocking && (cageSlice%LCM)%(LCM/shockRate)==0){
				ImageProcessor currentIp = imageCapture.capture().getProcessor();
				for(int cage=0;cage<allCage;cage++){
					if(existMice[cage]){
				        ((FZResultSaver)resSaver).setShockImage(cage, roiOperator.split(currentIp,cage));
					}
				}
		    }

		    if(allCage==1 && cageSlice%shockRate==0 && (cageSlice-1) != allSlice){
	            timer.run((double)cageSlice/shockRate);
			}
			//Timerで中断した場合
			if((timer != null && timer.isInterrupt()) || interrupt()){
		        isEnd();
			}

			if(endAnalyze) isEnd();

			cageSlice++;}catch(Throwable e){e.printStackTrace();}
		}

		@Override
		public void isEnd(){
			futures[0].cancel(true);
			finishSignal.countDown();

			//Timerで中断した場合
			if((timer != null && timer.isInterrupt()) || interrupt()){
				for(int cage=0;cage<allCage;cage++){
				    saveTraceInInterrupt(cage, cageSlice);
			        analyze[cage].interruptAnalysis();
				}
			}
		}

		private void checkShock(final int currentSlice){
			if(isT && (stopT+2) == (double)currentSlice / Parameter.getInt(FZParameter.shockCaptureRate)){
				isT=false;
			}
			if(isS && (stopS+2) == (double)currentSlice / Parameter.getInt(FZParameter.shockCaptureRate)){
				isS=false;
				isShocking = false;
			}

			if((shock.getStartTime()-2) == (double)currentSlice / Parameter.getInt(FZParameter.shockCaptureRate)){
				if(shock.getShockType().equals("s")){
					isShocking = true;
		 	    }
			}else if(shockUnits.size()>shockNO){
				if(shockUnits.get(shockNO).getStartTime()-shock.getStartTime()<=2
					 && (shockUnits.get(shockNO).getStartTime()-2) == (double)currentSlice/Parameter.getInt(FZParameter.shockCaptureRate)
					    && shockUnits.get(shockNO).getShockType().equals("s")){
					isShocking = true;
				}
			}

			if(shock.getStartTime() == (double)currentSlice / Parameter.getInt(FZParameter.shockCaptureRate)){
				shock(shock.getShockType());

				if(shock.getShockType().equals("t")){
					stopT = shock.getStartTime() + shock.getDuration();
				}else if(shock.getShockType().equals("s")){
					stopS = shock.getStartTime() + shock.getDuration();
		 	    }

				if(shockUnits.size()>shockNO){
					shock = shockUnits.get(shockNO++);

					if(shock.getStartTime() == (double)currentSlice / Parameter.getInt(FZParameter.shockCaptureRate)){
						shock(shock.getShockType());

						if(shock.getShockType().equals("t")){
							stopT = shock.getStartTime() + shock.getDuration();
						}else if(shock.getShockType().equals("s")){
							stopS = shock.getStartTime() + shock.getDuration();
				 	    }

						if(shockUnits.size()>shockNO){
							shock = shockUnits.get(shockNO++);
						}
					}
				}
			}

			//Shock停止
			if(stopT == (double)currentSlice/Parameter.getInt(FZParameter.shockCaptureRate)){	//shock停止時刻が来た場合
				stopShock("t");
			}
			if(stopS == (double)currentSlice/Parameter.getInt(FZParameter.shockCaptureRate)){
				stopShock("s");
			}
		}

		/********
		指定されたタイプに従ってアウトプットを出す
		Fear Conditioning では、アウトプットの値は
		1 .. 音の出力
		2 .. 電気刺激の出力
		である。
		 *********/
		private void shock(String type){
			System.out.println("Start at "+(cageSlice/4));
			if(type.compareTo("t") == 0){
				isT=true;
				output.controlOutput(1);
			}else if(type.compareTo("s") == 0){
				isS=true;
				output.controlOutput(2);
		    }
		}

		/**********
		指定されたタイプに従って、継続していたアウトプットを止める
		 ***********/
		private void stopShock(String type){
			System.out.println("Stop at "+(cageSlice/4));
			if(type.compareTo("t") == 0){
				output.clear(1);
			}else if(type.compareTo("s") == 0){
				output.clear(2);			
		    }
		}
	}

	@Override
	protected void end(){
		resSaver.writeDate(true, true, binFileName);
		if(!FileManager.getInstance().getReferenceFileName().equals("#NoUse")){
		    output.close();
		}
	}

	@Override
	protected void saveBinResult(boolean writeVersion) {
		int[] option = {FZAnalyzer.BIN_DISTANCE, FZAnalyzer.BIN_FREEZ_PERCENT};

		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++)
				result[cage] = ((FZAnalyzer)analyze[cage]).getBinResult(option[num]);
			resSaver.saveOnlineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		xorImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++){
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
			xorImp[cage] = new ImagePlus("xor" + (cage + 1), backIp[cage]);
		}
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN,existMice);
		winOperator.setImageWindow(xorImp, WindowOperator.RIGHT_UP,existMice);
	}

	@Override
	protected synchronized void setCurrentImage(int cage, int sliceNum, ImageProcessor currentIp){
		currentImp[cage].setProcessor(subjectID[cage], currentIp);
		resSaver.setCurrentImage(cage, currentIp);

		ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
		traceImp[cage].setProcessor("trace" + (cage + 1), traceIp);

		if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(FZParameter.shockCaptureRate)) == 0)
		    resSaver.addTraceImage(cage, traceIp);

		setEachCurrentImage(cage,sliceNum);	//abstract: それぞれの実験の画像表示
	}

	protected void setEachCurrentImage(int cage,int num){
		ImageProcessor sub = ((FZAnalyzer)analyze[cage]).getSubtractImage();
		subtractImp[cage].setProcessor("subtract" + (cage + 1), sub);
		if(sub!=null){
		    ((FZResultSaver)resSaver).setSubtractImage(cage, sub);
		}
		xorImp[cage].setProcessor("xor" + (cage + 1), ((FZAnalyzer)analyze[cage]).getXorImage());

		if(num!=0)
		xorImageStack[cage].addSlice("xor", ((FZAnalyzer)analyze[cage]).getXorImage().convertToByte(false));
	}

	@Override
	protected void setEachCurrentImage(int cage){}

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
		if(shockUnits.size()!=0){
			((FZResultSaver)resSaver).saveShockImage(backIp);
		}

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];
		for(int cage=0; cage<allCage; cage++){
			if(!existMice[cage]) continue;
			resSaver.saveXYResult(cage, ((FZAnalyzer)analyze[cage]).getXYText());
			totalResult[cage] = analyze[cage].getResults();
		}

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion, writeParameter);
	    winOperator.showOnlineTotalResult(program, allCage, endAnalyze, subjectID, totalResult);

		saveBinResult(writeVersion);
		saveOthers(subjectID, backIp);
	}

	//構造体のようなもの	
	private class ShockUnit implements Comparable{
		private String shockType;
		private double startTime;
		private double duration;

		public ShockUnit(String shockType,double startTime,double duration){
			this.shockType = shockType;
			this.startTime = startTime;
			this.duration = duration;
		}

		public String getShockType(){return shockType;}
		public double getStartTime(){return startTime;}
		public double getDuration(){return duration;}

		//開始時間が比較対象より早ければ負を返す
		@Override
		public int compareTo(Object arg0)throws ClassCastException,NullPointerException{
			if(startTime-((ShockUnit)arg0).getStartTime() == 0.0){
				if(shockType.equals(((ShockUnit)arg0).getShockType())){
					return 0;
				}else{
					return shockType.equals("t")? 1 : -1;
				}
			}else{
				return (int)(startTime-((ShockUnit)arg0).getStartTime());
			}
		}
	}
}